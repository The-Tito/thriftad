package com.polet.thriftadapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.polet.thriftadapp.R // Asegúrate de que este import coincida con tu paquete

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val channelId = "thriftad_push"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Configuración del Canal (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Thriftad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de presupuesto y gastos"
                enableLights(true)
                lightColor = Color.MAGENTA // Color del LED si el cel tiene
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Construcción de la Notificación Profesional
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // AQUÍ USAMOS TU PANDA
            .setSmallIcon(R.drawable.panda_logo)
            // Color morado/lila para el círculo del icono
            .setColor(ContextCompat.getColor(this, R.color.purple_200))
            .setContentTitle(remoteMessage.notification?.title ?: "Thriftad Alerta")
            .setContentText(remoteMessage.notification?.body ?: "¡Revisa tus finanzas, Polet!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            // Vibración y sonido por defecto
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // 3. Mostrar la notificación
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token listo para tu futuro backend en AWS
    }
}