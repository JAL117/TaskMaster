package com.example.taskmaster.APIService

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskmaster.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val _receivedMessage = MutableSharedFlow<Pair<String?, String?>>()
    val receivedMessage: SharedFlow<Pair<String?, String?>> = _receivedMessage.asSharedFlow()

    override fun onNewToken(token: String) {
        Log.d("FCM Token", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM Message", "From: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM Message", "Message data payload: " + remoteMessage.data)
            handleDataMessage(remoteMessage.data)
        }

        remoteMessage.notification?.let { notification ->
            Log.d("FCM Message", "Message Notification Body: ${notification.body}")
            showNotification(notification.title, notification.body)
            CoroutineScope(Dispatchers.IO).launch {
                _receivedMessage.emit(Pair(notification.title, notification.body))
            }
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "my_channel_id"
        val notificationId = 1

        // Crear el Intent para abrir MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir la notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Cambiar por un icono adecuado
            .setContentTitle(title ?: "Nueva Notificación")
            .setContentText(body ?: "Tienes un nuevo mensaje")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Crear el canal de notificación (para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Channel"
            val channelDescription = "Canal de notificaciones de mi aplicación"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Mostrar la notificación
        val notificationManagerCompat = NotificationManagerCompat.from(this)

        // Verificar si se tienen los permisos (Android 13+)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("FCM", "No se tienen los permisos para mostrar la notificación")
            // No mostrar la notificación si no se tienen los permisos
            return
        }

        notificationManagerCompat.notify(notificationId, builder.build())
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"]
        val message = data["message"]
        CoroutineScope(Dispatchers.IO).launch {
            _receivedMessage.emit(Pair(title, message))
        }
        Log.d("FCM Data Message", "Title: $title, Message: $message")
    }
    ///Si
    // Función para enviar el token al servidor (ahora pública y estática)
    companion object {
        fun sendRegistrationToServer(context: Context, token: String?) {
            if (token == null) return

            // Obtener el userId de SharedPreferences
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getInt("user_id", -1)
            println("El ID del user es $userId")
            if (userId == -1) {
                Log.w("FCM", "Usuario no autenticado, no se envía el token")
                return
            }

            val apiService = RetroClient.instance
            val request = RegisterFcmTokenRequest(userId, token)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = suspendCancellableCoroutine<Response<Unit>> { cancellableContinuation ->
                        val call = apiService.registerFcmToken(request)
                        call.enqueue(object : Callback<Unit> {
                            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                                cancellableContinuation.resume(response)
                            }

                            override fun onFailure(call: Call<Unit>, t: Throwable) {
                                cancellableContinuation.resumeWithException(t)
                            }
                        })

                        cancellableContinuation.invokeOnCancellation {
                            call.cancel()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Log.d("FCM", "Token enviado al servidor con éxito")
                        } else {
                            Log.e("FCM", "Error al enviar el token: ${response.message()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("FCM", "Error de red: ${e.message}")
                    }
                }
            }
        }
    }
}