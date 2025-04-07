package com.example.taskmaster // Asegúrate que este sea tu paquete raíz

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmaster.Menu.MenuScreen
import com.example.taskmaster.APIService.MyFirebaseMessagingService
//import com.example.taskmaster.Add_Edit_Task.AddEditTaskScreen
import com.example.taskmaster.Task_List.TaskListScreen
import com.example.taskmaster.Login.LoginScreen
import com.example.taskmaster.Register.RegisterScreen
import com.google.firebase.messaging.FirebaseMessaging
import com.example.taskmaster.ui.theme.TaskMasterTheme


class MainActivity : ComponentActivity() {

    private val notificationService = MyFirebaseMessagingService()
    private val REQUEST_CODE_POST_NOTIFICATIONS = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val notificationMessage = remember { mutableStateOf("") }
            // Usa la recolección del flujo con LaunchedEffect para un ciclo de vida seguro
            LaunchedEffect(Unit) {
                notificationService.receivedMessage.collect { (title, message) ->
                    notificationMessage.value = "Título: $title, Mensaje: $message"
                    Log.d("FCM_UI", "Mensaje de UI: ${notificationMessage.value}") // Log para depuración
                }
            }

            TaskMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column {
                        if (notificationMessage.value.isNotEmpty()) {
                            Text(
                                text = "Mensaje Push: ${notificationMessage.value}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = "login_screen"
                        ) {
                            composable("login_screen") {
                                LoginScreen(
                                    navController = navController,
                                    onLoginSuccess = {
                                        checkAndRequestNotificationPermission()
                                    }
                                )
                            }
                            composable("register_screen") {
                                RegisterScreen(navigateToLogin = {
                                    navController.navigate("login_screen")
                                })
                            }
                            composable("home_screen") {
                                MenuScreen(navController = navController)
                            }
                            //composable("add_task_screen") {
                            //    AddEditTaskScreen(navController = navController)
                            //}
                            //composable("task_list_screen") {
                            //    TaskListScreen(navController = navController)
                           // }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // El permiso no está garantizado, solicitarlo
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        } else {
            // Si ya tenemos el permiso, obtenemos el token directamente
            getToken()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, // Especifica el tipo como Array<String>
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_POST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permiso concedido
                    Log.d("FCM", "Permiso de notificaciones concedido")
                    getToken()
                } else {
                    // Permiso denegado
                    Log.d("FCM", "Permiso de notificaciones denegado")
                    // Puedes mostrar un mensaje al usuario explicando por qué necesitas el permiso
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("FCM", "FCM Token: $token")
            // Guarda este token y envíalo a tu servidor (NodeJS)
            // para que puedas enviar notificaciones dirigidas a este dispositivo.
            MyFirebaseMessagingService.sendRegistrationToServer(this, token)

            //Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        }
    }
}
