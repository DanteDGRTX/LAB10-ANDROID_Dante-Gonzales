# LAB10 Android Series CRUD

Aplicación Android con Jetpack Compose que consume la API REST de series del proyecto Django `djangoApp08` mediante Retrofit y Gson.

## Conexión al servicio web

El cliente está configurado en `app/src/main/java/com/dantegonzales/lab10android/data/ApiClient.kt`. En el emulador Small_Phone de este equipo, la URL que permitió conectar la aplicación es `http://192.168.1.6:8000/`. Si cambia la IP del PC, actualice `BASE_URL`. En una instalación donde el emulador pueda alcanzar al host por la dirección estándar, también puede usarse `http://10.0.2.2:8000/`. Para un teléfono físico, use la IP LAN del PC y conecte ambos dispositivos a la misma red.

En el PC, inicie Django desde la carpeta `lab08` del backend:

```powershell
python manage.py runserver 0.0.0.0:8000
```

La configuración de Django debe incluir la IP del PC en `ALLOWED_HOSTS`. Para que el laboratorio permita las llamadas de escritura sin iniciar sesión, las vistas de series deben aceptar las operaciones de la aplicación. Este servidor de desarrollo usa HTTP local; el manifiesto Android declara `INTERNET` y habilita tráfico HTTP en claro para esta práctica.

## Operaciones de la aplicación

`SerieApiService` declara las solicitudes Retrofit y `SerieModel` mapea los campos JSON (`release_date` se transforma a `releaseDate` en Kotlin):

| Acción en la app | Solicitud HTTP | Resultado |
| --- | --- | --- |
| Abrir/actualizar lista | `GET /serie` | Descarga las series y actualiza la lista Compose. |
| Abrir una serie para editar | `GET /serie/{id}` | Obtiene los datos actuales antes de mostrar el formulario. |
| Guardar una serie nueva | `POST /serie` | Crea el registro y vuelve a cargar la lista. |
| Guardar cambios | `PUT /serie/{id}` | Actualiza los campos y vuelve a cargar la lista. |
| Eliminar | `DELETE /serie/{id}` | Pide confirmación, elimina el registro y actualiza la lista. |

La pantalla ejecuta las funciones de red como `suspend` usando coroutines, por lo que no bloquea el hilo de interfaz. Los estados Compose muestran carga, resultados, errores de conexión y confirmaciones. El formulario valida que el nombre no esté vacío, la fecha tenga formato `AAAA-MM-DD`, la calificación esté entre 0 y 10 y la categoría sea `horror`, `comedy`, `action` o `drama`.

## Ejecutar y probar

1. Inicie el servidor Django en el PC y confirme que responde en `http://192.168.1.6:8000/serie`.
2. Abra este proyecto en Android Studio y ejecute la configuración `app` en un emulador Android.
3. En **Series**, use **Actualizar** para consultar el servidor. **Nueva serie** abre el formulario; **Editar** carga el registro seleccionado; **Eliminar** solicita confirmación antes del `DELETE`.
4. Para compilar desde la terminal de Windows:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

En la verificación de esta entrega, la app creó `CRUD Demo` (ID 4, fecha `2020-05-05`), actualizó la fecha a `2020-05-06` y eliminó ese registro desde el diálogo de confirmación. El ID 2 se había eliminado previamente al seguir el paso DELETE de la guía; por eso no se usa en esta prueba de CRUD.

## Bookshelf

La pantalla **Bookshelf** está disponible desde el botón superior de la cartelera. Busca libros en Google Books con Retrofit y Gson, obtiene el detalle de cada volumen y carga sus portadas de forma asíncrona con Coil. La capa `BooksRepository` separa la pantalla del servicio de red y permite inyectar repositorios falsos para pruebas.

Google Books devolvió HTTP 429 (cuota diaria excedida) durante la última ejecución en el emulador. Para que la pantalla siga mostrando resultados, la app usa Open Library como respaldo y muestra la fuente de los resultados en la cuadrícula. Cuando Google Books responda, se usa como fuente principal automáticamente.

El repositorio y su prueba unitaria están en `app/src/main/java/com/dantegonzales/lab10android/books` y `app/src/test/java/com/dantegonzales/lab10android/books`. Para compilar y ejecutar la prueba:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```
