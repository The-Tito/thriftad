# ThriftAd — Frontend (Android)

Aplicación Android escrita en **Kotlin + Jetpack Compose**. Gestión de finanzas personales con captura de tickets por cámara, gráficas de gastos y metas de ahorro.

## Requisitos

| Herramienta | Versión |
|---|---|
| Android Studio | Hedgehog o superior |
| JDK | 17 (incluido en Android Studio) |
| SDK mínimo | API 26 (Android 8.0) |
| SDK objetivo | API 35 |

## Configuración inicial

1. Abrir la carpeta `frontend/` en Android Studio.
2. Esperar a que Gradle sincronice las dependencias.
3. Si la URL del backend cambia, editarla en:
   `app/src/main/java/com/polet/thriftadapp/di/NetworkModule.kt`
   ```kotlin
   // Emulador → usa 10.0.2.2 (localhost del host)
   baseUrl = "http://10.0.2.2:8080/"
   // Dispositivo real → IP local del PC en la red WiFi
   baseUrl = "http://192.168.1.X:8080/"
   ```
4. Asegurarse de que el backend está corriendo antes de lanzar la app.

## Compilar y ejecutar

```
Android Studio → Run ▶  (Shift+F10)
```

O desde terminal:
```bash
./gradlew assembleDebug          # APK debug en app/build/outputs/apk/debug/
./gradlew installDebug           # Instalar en emulador/dispositivo conectado
./gradlew test                   # Tests unitarios
```

## Arquitectura

```
app/src/main/java/com/polet/thriftadapp/
├── data/
│   ├── local/
│   │   ├── dao/          # TicketDao, GoalDao, HiddenTransactionDao
│   │   ├── database/     # AppDatabase (Room v7)
│   │   └── entities/     # TicketEntity, GoalEntity, HiddenTransactionEntity
│   └── remote/           # ApiService (Retrofit)
├── di/                   # AppModule, NetworkModule (Hilt)
├── domain/
│   ├── model/            # DTOs: HomeResponse, MovimientoRequest, etc.
│   └── use_case/         # GetHomeDataUseCase
└── presentation/
    ├── screens/
    │   ├── camera/       # Captura de tickets
    │   ├── gallery/      # Galería de tickets con imagen
    │   ├── home/         # Home: balance, gráfica, movimientos
    │   ├── login/        # Login y registro
    │   ├── places/       # Mapa con ubicación de gastos
    │   └── settings/     # Configuración de cuenta
    └── viewmodels/       # HomeViewModel, CameraViewModel, PlacesViewModel, etc.
```

## Pantallas principales

| Pantalla | Descripción |
|---|---|
| Home | Balance, gráfica de gastos por categoría, listado de movimientos recientes |
| Cámara | Captura ticket → extrae concepto/monto → sincroniza con backend |
| Galería | Historial de tickets con imagen de Cloudinary |
| Lugares | Mapa con pins de dónde se registraron los gastos |
| Configuración | Editar nombre y otros datos de cuenta |

## Roles de usuario

| Rol | Categorías de gasto |
|---|---|
| Hogar | Alimentación, Transporte, Entretenimiento, Salud, Ropa, Hogar |
| Estudiante | + Materiales de estudio, Educación |
| Negocio | Ventas, Compras, Gastos operacionales, Salarios, Impuestos, Servicios |

## Base de datos local (Room)

- **tickets** — tickets capturados por cámara (con imagen local y cloudinaryUrl)
- **goals** — metas de ahorro por usuario
- **hidden_transactions** — IDs de movimientos ocultados en Home (swipe)

Versión actual: **7**. Migraciones en `AppDatabase.kt`.
