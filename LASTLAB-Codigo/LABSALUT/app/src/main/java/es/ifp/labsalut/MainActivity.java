package es.ifp.labsalut;

import static es.ifp.labsalut.ui.SettingsFragment.MY_PREFS_HUELLA;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.view.GravityCompat;

import javax.crypto.SecretKey;

import es.ifp.labsalut.db.BaseDatos;
import es.ifp.labsalut.negocio.Usuario;
import es.ifp.labsalut.seguridad.CifradoAES;
import es.ifp.labsalut.seguridad.FingerprintHandler;
import es.ifp.labsalut.ui.HomeFragment;
import es.ifp.labsalut.ui.RegistroActivity;
import es.ifp.labsalut.ui.SettingsFragment;

public class MainActivity extends AppCompatActivity implements FingerprintHandler.AuthenticationCallback {

    public static final String MY_PREFS_USER = "RECORDARUSUARIO";
    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button loginButton;
    protected Button registroButton;
    protected TextView titleLabel;
    protected CheckBox recordarUser;
    private Intent pasarPantalla;
    private String email;
    private String password;
    private BaseDatos db;
    private Usuario user = null;
    private FingerprintHandler finger = null;
    private boolean activacionHuella = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new BaseDatos(this);
        // Obtener referencias de los elementos de la interfaz
        titleLabel = (TextView) findViewById(R.id.logo_main);
        emailEditText = (EditText) findViewById(R.id.userEmail_main);
        passwordEditText = (EditText) findViewById(R.id.password_main);
        loginButton = (Button) findViewById(R.id.acceptButton_main);
        registroButton = (Button) findViewById(R.id.newUserBoton_main);
        recordarUser = (CheckBox) findViewById(R.id.recordarUsuario_main);

        // Configurar el título de la aplicación centrado en un fondo negro y blanco
        titleLabel.setBackgroundColor(getResources().getColor(R.color.black));
        titleLabel.setTextColor(getResources().getColor(R.color.white));
        SharedPreferences prefs_user = getSharedPreferences(MY_PREFS_USER, MODE_PRIVATE);
        SharedPreferences prefs_huella = getSharedPreferences(MY_PREFS_HUELLA, MODE_PRIVATE);
        SharedPreferences.Editor editor_user = getSharedPreferences(MY_PREFS_USER, MODE_PRIVATE).edit();

        String restoredText = prefs_user.getString("EMAIL", null);
        if (restoredText != null) {

            recordarUser.setChecked(true);
            String email = prefs_user.getString("EMAIL", "");
            String password = prefs_user.getString("PASS", "");
            emailEditText.setText(email);
            passwordEditText.setText(password);

            String huellaActiva = prefs_user.getString("FINGER", null);
            if (huellaActiva.equals("SI")) {
                activacionHuella = false;
                try {
                    user = validarCredenciales(db, email, password);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    finger = new FingerprintHandler(this, this);
                    finger.startAuth();
                }

            }
        } else {
            recordarUser.setChecked(false);
        }

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Los campos Usuario y Contraseña no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                } else {
                    // Verificar si el usuario y la contraseña son correctos
                    try {
                        user = validarCredenciales(db, email, password);
                        if (user != null) {
                            if (recordarUser.isChecked()) {
                                editor_user.putString("EMAIL", user.getEmail());
                                editor_user.putString("PASS", user.getContrasena());
                            } else {
                                editor_user.putString("EMAIL", null);
                                editor_user.putString("PASS", null);
                            }
                            editor_user.apply();

                            String primeravezHuella = prefs_huella.getString("PRIMERAVEZ" + user.getNombre(), null);

                            if (primeravezHuella!=null) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("¿Quiere activar la huella digital para iniciar sesión?");
                                builder.setMessage("");
                                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                            activacionHuella = true;
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    FingerprintHandler fingerFirst = new FingerprintHandler(MainActivity.this, MainActivity.this);
                                                    fingerFirst.startAuth();
                                                }
                                            });
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        pasarPantalla = new Intent(MainActivity.this, MenuActivity.class);
                                        pasarPantalla.putExtra("USUARIO", user);
                                        activacionHuella = false;
                                        finish();
                                        startActivity(pasarPantalla);
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            } else {
                                pasarPantalla = new Intent(MainActivity.this, MenuActivity.class);
                                pasarPantalla.putExtra("USUARIO", user);
                                activacionHuella = false;
                                String huella = prefs_huella.getString("HUELLA" + user.getNombre(), null);
                                if (huella!=null) {
                                    editor_user.putString("FINGER", "SI");
                                }
                                editor_user.apply();
                                finish();
                                startActivity(pasarPantalla);
                            }
                        } else {
                            // Si no son correctos, mostrar un mensaje de error
                            Toast.makeText(MainActivity.this, "El usuario o la contraseña no es correcta", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        registroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pasarPantalla = new Intent(MainActivity.this, RegistroActivity.class);
                finish();
                startActivity(pasarPantalla);
            }
        });

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    // Método para verificar las credenciales del usuario
    private Usuario validarCredenciales(BaseDatos db, String email, String password) throws Exception {
        // Lógica para verificar si el email y la contraseña son correctos

        Usuario usuario = null;

        CifradoAES aes = new CifradoAES();
        String semilla = email + password;
        SecretKey secretKey = aes.generarSecretKey(semilla);
        String encrypt = null;
        String encrypt2 = null;
        try {
            encrypt = aes.encrypt(password.getBytes(), secretKey);
            encrypt2 = aes.encrypt(email.getBytes(), secretKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        usuario = db.getUser(encrypt2);
        if (encrypt.equals(usuario.getContrasena()) &&
                encrypt2.equals(usuario.getEmail())) {
            usuario.setIdUsuario(usuario.getIdUsuario());
            usuario.setNombre(aes.decrypt(usuario.getNombre(), secretKey));
            usuario.setFechaNacimiento(aes.decrypt(usuario.getFechaNacimiento(), secretKey));
            usuario.setEmail(aes.decrypt(usuario.getEmail(), secretKey));
            usuario.setContrasena(aes.decrypt(usuario.getContrasena(), secretKey));
        } else {
            usuario = null;
        }

        return usuario;
    }

    @Override
    public void onAuthenticationSucceeded() {
        SharedPreferences.Editor editor_huella = getSharedPreferences(MY_PREFS_HUELLA, MODE_PRIVATE).edit();
        SharedPreferences.Editor editor_user = getSharedPreferences(MY_PREFS_USER, MODE_PRIVATE).edit();
        if (activacionHuella) {
            editor_huella.putString("HUELLA" + user.getNombre(), "SI");
            editor_huella.putString("PRIMERAVEZ" + user.getNombre(), null);
            editor_user.putString("FINGER", "SI");
            activacionHuella = false;
        }
        editor_huella.apply();
        editor_user.apply();
        pasarPantalla = new Intent(MainActivity.this, MenuActivity.class);
        pasarPantalla.putExtra("USUARIO", user);
        finish();
        startActivity(pasarPantalla);
    }

    @Override
    public void onAuthenticationFailed() {

    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        // Handle authentication error
        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
            if (activacionHuella) {
                pasarPantalla = new Intent(MainActivity.this, MenuActivity.class);
                pasarPantalla.putExtra("USUARIO", user);
                finish();
                startActivity(pasarPantalla);
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Vuelva a intentarlo en 30 segundos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}