// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.apushnikov.other351_mytelegram.cropper;

import static com.apushnikov.other351_mytelegram.myApplication.MyApplication.logFile;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

//import com.apushnikov.other361_sample_image_cropper.R;

import com.apushnikov.other351_mytelegram.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**=======================================================================================
 * Помощник, упрощающий работу с кадрированием изображения, например запуск действий по выбору
 * изображения и обработку намерений камеры/галереи.<br>
 * Цель помощника состоит в том, чтобы упростить начальное и наиболее частое использование
 * обрезки изображений, а не предлагать все возможные сценарии кодовой базы «один для всех».
 * Так что не стесняйтесь использовать его как есть и как вики, чтобы создавать свои собственные.<br>
 * Добавленная ценность, которую вы получаете «из коробки», — это некоторая обработка крайних случаев,
 * которую вы можете пропустить в противном случае, например, дурацкий URI результата
 * камеры Android, который может отличаться от версии к версии и от устройства к устройству.
 * <br>
 * <br>
 * Helper to simplify crop image work like starting pick-image acitvity and handling camera/gallery
 * intents.<br>
 * The goal of the helper is to simplify the starting and most-common usage of image cropping and
 * not all porpose all possible scenario one-to-rule-them-all code base. So feel free to use it as
 * is and as a wiki to make your own.<br>
 * Added value you get out-of-the-box is some edge case handling that you may miss otherwise, like
 * the stupid-ass Android camera result URI that may differ from version to version and from device
 * to device.
 */
@SuppressWarnings("WeakerAccess, unused")
public final class CropImage {

  //===========================================================================================
  // region: Поля и константы
  //===========================================================================================

  /** Ключ, используемый для передачи URI источника кадрированного изображения в {@link CropImageActivity}. */
  public static final String CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";

  /** Ключ, используемый для передачи параметров обрезки изображения в {@link CropImageActivity}. */
  public static final String CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";

  /** Ключ, используемый для передачи данных пакета изображений обрезки в {@link CropImageActivity}. */
  public static final String CROP_IMAGE_EXTRA_BUNDLE = "CROP_IMAGE_EXTRA_BUNDLE";

  /** Ключ, используемый для передачи данных результата кадрирования обратно из {@link CropImageActivity}. */
  public static final String CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT";

  /** Код запроса, используемый для начала действия по выбору изображения, который будет
   * использоваться в результате для идентификации этого конкретного запроса.*/
  public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 200;

  /** Код запроса, используемый для запроса разрешения на выбор изображения из внешнего хранилища. */
  public static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 201;

  /** Код запроса, используемый для запроса разрешения на захват изображения с камеры. */
  public static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = 2011;

  /** Код запроса, используемый для запуска {@link CropImageActivity}, будет использоваться
   * в результате для идентификации этого конкретного запроса. */
  public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;

  /** Код результата, используемый для возврата ошибки из {@link CropImageActivity}. */
  public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;

  // endregion

  //===========================================================================================
  // region: Методы
  //===========================================================================================

  /**======================================================================================
   * CropImage - пустой конструктор
   */
  private CropImage() {}

  /**======================================================================================
   * Создайте новое растровое изображение, в котором все пиксели за пределами овальной
   * формы прозрачны. Старый битмап перерабатывается.
   */
  public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(output);

    int color = 0xff424242;
    Paint paint = new Paint();

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);

    RectF rect = new RectF(0, 0, width, height);
    canvas.drawOval(rect, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, 0, 0, paint);

    bitmap.recycle();

    return output;
  }

  /**========================================================================================
   * startPickImageActivity - Запустите активити, чтобы получить изображение для обрезки с
   * помощью средства выбора, которое будет иметь все доступные приложения для устройства, такие как<br>
   * - камера (MyCamera),<br>
   * - галерея (Photos),<br>
   * - магазин приложений (Dropbox).<br>
   * Используйте строковый ресурс «pick_image_intent_chooser_title»,
   * чтобы переопределить заголовок средства выбора.
   *
   * @param activity активити, которое будет использоваться для запуска действия из
   */
  public static void startPickImageActivity(@NonNull Activity activity) {
    // Запускаем активность с получением результата
    activity.startActivityForResult(
            getPickImageChooserIntent(activity), PICK_IMAGE_CHOOSER_REQUEST_CODE);
  }

  /**=========================================================================================
   * startPickImageActivity - То же, что и
   * метод {@link #startPickImageActivity(Activity) startPickImageActivity}, но вместо того,
   * чтобы вызываться и возвращаться к действию, этот метод может вызываться и возвращаться к фрагменту.
   * <br>
   * Same as {@link #startPickImageActivity(Activity) startPickImageActivity} method but instead of
   * being called and returning to an Activity, this method can be called and return to a Fragment.
   *
   * @param context Контекст фрагментов. Используйте getContext()
   * @param fragment Вызывающий фрагмент для запуска и возврата изображения в
   */
  public static void startPickImageActivity(@NonNull Context context, @NonNull Fragment fragment) {
    // Запускаем активность с получением результата
    fragment.startActivityForResult(
            getPickImageChooserIntent(context), PICK_IMAGE_CHOOSER_REQUEST_CODE);
  }

  /**========================================================================================
   * getPickImageChooserIntent - Создайте намерение средства выбора, чтобы выбрать источник для
   * получения изображения.<br>
   * Источником может быть: <br>
   * - камера (ACTION_IMAGE_CAPTURE) или <br>
   * - галерея (ACTION_GET_CONTENT).<br>
   * Все возможные источники добавляются в окно выбора намерений.<br>
   * Используйте строковый ресурс «pick_image_intent_chooser_title», чтобы переопределить заголовок средства выбора.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   */
  public static Intent getPickImageChooserIntent(@NonNull Context context) {
    return getPickImageChooserIntent(
            context, context.getString(R.string.pick_image_intent_chooser_title), false, true);
  }

  /**====================================================================================
   * getPickImageChooserIntent - Создайте намерение средства выбора, чтобы выбрать источник
   * для получения изображения.<br>
   * Источником может быть:<br>
   * - камера (ACTION_IMAGE_CAPTURE) или<br>
   * - галерея (ACTION_GET_CONTENT).<br>
   * Все возможные источники добавляются в средство выбора намерений.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   * @param title название, используемое для пользовательского интерфейса выбора
   * @param includeDocuments если включить активность документов KitKat, содержащую все источники
   * @param includeCamera если включить намерения камеры
   */
  public static Intent getPickImageChooserIntent(
          @NonNull Context context,
          CharSequence title,
          boolean includeDocuments,
          boolean includeCamera) {

    List<Intent> allIntents = new ArrayList<>();
    PackageManager packageManager = context.getPackageManager();

    // собрать все намерения камеры, если доступно разрешение камеры
    if (!isExplicitCameraPermissionRequired(context) && includeCamera) {
      allIntents.addAll(getCameraIntents(context, packageManager));
    }

    List<Intent> galleryIntents =
            getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, includeDocuments);
    if (galleryIntents.size() == 0) {
      // если намерения для get-content не найдены, попробуйте выбрать действие намерения (Huawei P9).
      // if no intents found for get-content try pick intent action (Huawei P9).
      galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_PICK, includeDocuments);
    }
    allIntents.addAll(galleryIntents);

    Intent target;
    if (allIntents.isEmpty()) {
      target = new Intent();
    } else {
      target = allIntents.get(allIntents.size() - 1);
      allIntents.remove(allIntents.size() - 1);
    }

    // Создайте средство выбора из основного намерения
    Intent chooserIntent = Intent.createChooser(target, title);

    // Добавить все остальные намерения
    chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

    return chooserIntent;
  }

  /**==========================================================================================
   * getCameraIntent - Получите основное намерение камеры для захвата изображения с помощью
   * приложения камеры устройства. Если outputFileUri имеет значение null, Uri по умолчанию будет
   * создан с помощью {@link #getCaptureImageOutputUri(Context)}, поэтому вы сможете
   * получить pictureUri с помощью {@link #getPickImageResultUri(Context, Intent)}.
   * В противном случае вы просто используете Uri, переданный этому методу.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   * @param outputFileUri Uri, где будет размещена картинка.
   */
  public static Intent getCameraIntent(@NonNull Context context, Uri outputFileUri) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (outputFileUri == null) {
      outputFileUri = getCaptureImageOutputUri(context);
    }
    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    return intent;
  }

  /**========================================================================================
   * getCameraIntents - Получите все намерения камеры для захвата изображения с
   * помощью приложений камеры устройства
   *
   * @param context
   * @param packageManager
   * @return
   */
  public static List<Intent> getCameraIntents(
          @NonNull Context context, @NonNull PackageManager packageManager) {

    List<Intent> allIntents = new ArrayList<>();

    // Определите Uri изображения камеры для сохранения.
    Uri outputFileUri = getCaptureImageOutputUri(context);

    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
    for (ResolveInfo res : listCam) {
      Intent intent = new Intent(captureIntent);
      intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
      intent.setPackage(res.activityInfo.packageName);
      if (outputFileUri != null) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
      }
      allIntents.add(intent);
    }

    return allIntents;
  }

  /**=======================================================================================
   * getGalleryIntents - Получите все намерения Галереи для получения изображения из
   * одного из приложений устройства, которые обрабатывают изображения.
   */
  public static List<Intent> getGalleryIntents(
          @NonNull PackageManager packageManager, String action, boolean includeDocuments) {
    List<Intent> intents = new ArrayList<>();
    Intent galleryIntent =
            action == Intent.ACTION_GET_CONTENT
                    ? new Intent(action)
                    : new Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    galleryIntent.setType("image/*");
    List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
    for (ResolveInfo res : listGallery) {
      Intent intent = new Intent(galleryIntent);
      intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
      intent.setPackage(res.activityInfo.packageName);
      intents.add(intent);
    }

    // удалить намерение документов
    if (!includeDocuments) {
      for (Intent intent : intents) {
        if (intent
                .getComponent()
                .getClassName()
                .equals("com.android.documentsui.DocumentsActivity")) {
          intents.remove(intent);
          break;
        }
      }
    }
    return intents;
  }

  /**=========================================================================================
   * isExplicitCameraPermissionRequired - Проверьте, требуется ли явный запрос разрешения камеры.<br>
   * Это требуется в Android Marshmallow и более поздних версиях, если в манифесте запрашивается
   * разрешение "КАМЕРА".<br>
   * See <a
   * href="http://stackoverflow.com/questions/32789027/android-m-camera-intent-permission-bug">StackOverflow
   * question</a>.
   */
  public static boolean isExplicitCameraPermissionRequired(@NonNull Context context) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && hasPermissionInManifest(context, "android.permission.CAMERA")
            && context.checkSelfPermission(Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED;
  }

  /**==========================================================================================
   * hasPermissionInManifest - Проверьте, запрашивает ли приложение определенное разрешение в манифесте.
   *
   * @param permissionName разрешение на проверку
   * @return true — разрешение запрашивается в манифесте, false — нет.
   */
  public static boolean hasPermissionInManifest(
          @NonNull Context context, @NonNull String permissionName) {
    String packageName = context.getPackageName();
    try {
      PackageInfo packageInfo =
              context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
      final String[] declaredPermisisons = packageInfo.requestedPermissions;
      if (declaredPermisisons != null && declaredPermisisons.length > 0) {
        for (String p : declaredPermisisons) {
          if (p.equalsIgnoreCase(permissionName)) {
            return true;
          }
        }
      }
    } catch (PackageManager.NameNotFoundException e) {
    }
    return false;
  }

  /**========================================================================================
   * getCaptureImageOutputUri - Получить URI изображения, полученного при захвате камерой.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   */
  public static Uri getCaptureImageOutputUri(@NonNull Context context) {
    Uri outputFileUri = null;
    File getImage = context.getExternalCacheDir();
    if (getImage != null) {
      outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
    }
    return outputFileUri;
  }

  /**======================================================================================
   * getPickImageResultUri - Получите URI выбранного изображения из {@link #getPickImageChooserIntent(Context)}.<br>
   * Вернет правильный URI для камеры и изображения галереи.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   * @param data возвращаемые данные результата действия
   */
  public static Uri getPickImageResultUri(@NonNull Context context, @Nullable Intent data) {
    boolean isCamera = true;
    if (data != null && data.getData() != null) {
      String action = data.getAction();
      isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
    }
    return isCamera || data.getData() == null ? getCaptureImageOutputUri(context) : data.getData();
  }

  /**========================================================================================
   * isReadExternalStoragePermissionsRequired - Проверьте, требуются ли для выбранного URI
   * изображения разрешения READ_EXTERNAL_STORAGE.<br>
   * Релевантно только для API версии 23 и выше и не требуется для всех URI,
   * зависит от реализации приложения, которое использовалось для выбора изображения.<br>
   * Поэтому мы просто проверяем, можем ли мы открыть поток или мы получаем исключение при попытке,
   * Android великолепен.
   * <br><br>
   * Check if the given picked image URI requires READ_EXTERNAL_STORAGE permissions.<br>
   * Only relevant for API version 23 and above and not required for all URI's depends on the
   * implementation of the app that was used for picking the image. So we just test if we can open
   * the stream or do we get an exception when we try, Android is awesome.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   * @param uri результирующий URI выбора изображения.
   * @return true — требуемые разрешения не предоставляются,
   * false — либо разрешения не требуются, либо они даны
   */
  public static boolean isReadExternalStoragePermissionsRequired(
          @NonNull Context context, @NonNull Uri uri) {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
            && isUriRequiresPermissions(context, uri);
  }

  /**==========================================================================================
   * isUriRequiresPermissions - Проверьте, можем ли мы открыть данный Android URI, чтобы проверить,
   * возникает ли ошибка, требующая разрешения.<br>
   * Актуально только для API версии 23 и выше.
   *
   * @param context используется для доступа к Android API, например для разрешения содержимого,
   *                это ваша активность/фрагмент/виджет.
   * @param uri результирующий URI выбора изображения.
   */
  public static boolean isUriRequiresPermissions(@NonNull Context context, @NonNull Uri uri) {
    try {
      ContentResolver resolver = context.getContentResolver();
      InputStream stream = resolver.openInputStream(uri);
      if (stream != null) {
        stream.close();
      }
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  /**========================================================================================
   * activity - Создайте экземпляр {@link ActivityBuilder}, чтобы открыть средство выбора изображений
   * для обрезки, а затем запустите {@link CropImageActivity}, чтобы обрезать
   * выбранное изображение.<br>
   * Результат будет получен
   * в {@link Activity->onActivityResult(int, int, Intent)} и может быть получен с
   * помощью {@link #getActivityResult(Intent)}.
   *
   * @return построитель для Crop Image Activity
   */
  public static ActivityBuilder activity() {
    return new ActivityBuilder(null);
  }

  /**=======================================================================================
   * activity - Создайте экземпляр {@link ActivityBuilder}, чтобы запустить {@link CropImageActivity}
   * для обрезки данного изображения.<br>
   * Результат будет получен в {@link Activity->onActivityResult(int, int, Intent)} и
   * может быть получен с помощью {@link #getActivityResult(Intent)}.
   *
   * @param uri источник изображения Android uri для обрезки или null, чтобы запустить средство выбора
   * @return builder для действий по обрезке изображений
   */
  public static ActivityBuilder activity(@Nullable Uri uri) {
    return new ActivityBuilder(uri);
  }

  /**=======================================================================================
   * getActivityResult - Получите объект данных результата {@link CropImageActivity} для действий
   * по обрезке изображений, запущенных с помощью {@link #activity(Uri)}.
   *
   * @param data намерение данных результата, полученное в {@link Activity->onActivityResult(int, int, Intent)}.
   * @return Обрезать объект результата действия изображения или ноль, если его не существует
   */
  public static ActivityResult getActivityResult(@Nullable Intent data) {
    return data != null ? (ActivityResult) data.getParcelableExtra(CROP_IMAGE_EXTRA_RESULT) : null;
  }

  // endregion

  //=============================================================================
  // region: Внутренний класс: ActivityBuilder
  //=============================================================================

  /** ActivityBuilder - Построитель, используемый для создания действия Crop Image
   * по запросу пользователя. */
  public static final class ActivityBuilder {

    /** mSource - Изображение для обрезки исходного кода Android uri. */
    @Nullable private final Uri mSource;

    /** mOptions - Опции для обрезки изображения UX */
    private final CropImageOptions mOptions;

    /**=======================================================================================
     * ActivityBuilder - конструктор
     *
     * @param source Изображение для обрезки исходного кода Android uri
     */
    private ActivityBuilder(@Nullable Uri source) {
      mSource = source;
      mOptions = new CropImageOptions();
    }

    /**=======================================================================================
     * getIntent - Получите намерение {@link CropImageActivity}, чтобы начать действие. */
    public Intent getIntent(@NonNull Context context) {
      return getIntent(context, CropImageActivity.class);
    }

    /**=======================================================================================
     * getIntent - Получите намерение {@link CropImageActivity}, чтобы начать действие. */
    public Intent getIntent(@NonNull Context context, @Nullable Class<?> cls) {

      // Убедитесь, что все параметры находятся в допустимом диапазоне.
      mOptions.validate();

      Intent intent = new Intent();
      intent.setClass(context, cls);
      // Bundle - Сопоставление ключей String с различными значениями Parcelable
      Bundle bundle = new Bundle();
      // В bundle вставляем uri - mSource - Изображение для обрезки исходного кода Android uri.
      bundle.putParcelable(CROP_IMAGE_EXTRA_SOURCE, mSource);
      // В bundle вставляем mOptions - Опции для обрезки изображения UX
      bundle.putParcelable(CROP_IMAGE_EXTRA_OPTIONS, mOptions);
      // В интент помещаем созданный bundle
      intent.putExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE, bundle);
      return intent;
    }

    /**========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param activity деятельность для получения результата
     */
    public void start(@NonNull Activity activity) {
      mOptions.validate();

      Intent intent = getIntent(activity);
      activity.startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);

/*      logFile.writeLogFile("CropImage: start(@NonNull Activity activity): Запускаем activity.startActivityForResult");
      activity.startActivityForResult(getIntent(activity), CROP_IMAGE_ACTIVITY_REQUEST_CODE);*/
    }

    /**========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param activity деятельность для получения результата
     */
    public void start(@NonNull Activity activity, @Nullable Class<?> cls) {

      mOptions.validate();
      activity.startActivityForResult(getIntent(activity, cls), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param fragment фрагмент для получения результата
     */
    public void start(@NonNull Context context, @NonNull Fragment fragment) {

      Intent intent = getIntent(context);

      fragment.startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);
//      fragment.startActivityForResult(getIntent(context), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**=========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param fragment фрагмент для получения результата
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void start(@NonNull Context context, @NonNull android.app.Fragment fragment) {
      fragment.startActivityForResult(getIntent(context), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**=========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param fragment фрагмент для получения результата
     */
    public void start(
            @NonNull Context context, @NonNull Fragment fragment, @Nullable Class<?> cls) {
      fragment.startActivityForResult(getIntent(context, cls), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**=========================================================================================
     * start - Запустите {@link CropImageActivity}.
     *
     * @param fragment фрагмент для получения результата
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void start(
            @NonNull Context context, @NonNull android.app.Fragment fragment, @Nullable Class<?> cls) {
      fragment.startActivityForResult(getIntent(context, cls), CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /**========================================================================================
     * setCropShape - Форма окна обрезки.<br>
     * Чтобы установить квадратную или круглую форму обрезки, установите соотношение сторон 1:1.<br>
     * <i>По умолчанию: RECTANGLE (ПРЯМОУГОЛЬНИК)</i>
     */
    public ActivityBuilder setCropShape(@NonNull CropImageView.CropShape cropShape) {
      mOptions.cropShape = cropShape;
      return this;
    }

    /**========================================================================================
     * setSnapRadius - Край окна обрезки будет привязан к соответствующему краю указанной
     * ограничивающей рамки, если край окна обрезки меньше или равен этому расстоянию (в пикселях)
     * от края ограничивающей рамки (в пикселях).<br>
     * <i>По умолчанию: 3dp</i>
     */
    public ActivityBuilder setSnapRadius(float snapRadius) {
      mOptions.snapRadius = snapRadius;
      return this;
    }

    /**========================================================================================
     * setTouchRadius - Радиус сенсорной области вокруг маркера (в пикселях).<br>
     * Мы основываем это значение на рекомендуемом ритме 48 dp.<br>
     * См. http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm<br>
     * <i>По умолчанию: 48dp</i>
     */
    public ActivityBuilder setTouchRadius(float touchRadius) {
      mOptions.touchRadius = touchRadius;
      return this;
    }

    /**========================================================================================
     * setGuidelines - должны ли направляющие быть включенными, отключенными или
     * отображаться только при изменении размера.<br>
     * <i>По умолчанию: ON_TOUCH</i>
     */
    public ActivityBuilder setGuidelines(@NonNull CropImageView.Guidelines guidelines) {
      mOptions.guidelines = guidelines;
      return this;
    }

    /**========================================================================================
     * setScaleType - Тип начального масштаба изображения в представлении обрезанного изображения<br>
     * <i>По умолчанию: FIT_CENTER</i>
     */
    public ActivityBuilder setScaleType(@NonNull CropImageView.ScaleType scaleType) {
      mOptions.scaleType = scaleType;
      return this;
    }

    /**========================================================================================
     * setShowCropOverlay - если показать пользовательский интерфейс наложения обрезки, который содержит
     * пользовательский интерфейс окна обрезки, окруженный фоном поверх изображения обрезки.<br>
     * <i>По умолчанию: true, может отключить анимацию или переход кадров.</i>
     */
    public ActivityBuilder setShowCropOverlay(boolean showCropOverlay) {
      mOptions.showCropOverlay = showCropOverlay;
      return this;
    }

    /**========================================================================================
     * setAutoZoomEnabled - если функция автоматического масштабирования включена.<br>
     * По умолчанию: true.
     */
    public ActivityBuilder setAutoZoomEnabled(boolean autoZoomEnabled) {
      mOptions.autoZoomEnabled = autoZoomEnabled;
      return this;
    }

    /**========================================================================================
     * setMultiTouchEnabled - если включена функция мультитач.<br>
     * По умолчанию: true.
     */
    public ActivityBuilder setMultiTouchEnabled(boolean multiTouchEnabled) {
      mOptions.multiTouchEnabled = multiTouchEnabled;
      return this;
    }

    /**========================================================================================
     * setMaxZoom - Максимально допустимое масштабирование при кадрировании.<br>
     * <i>По умолчанию: 4</i>
     */
    public ActivityBuilder setMaxZoom(int maxZoom) {
      mOptions.maxZoom = maxZoom;
      return this;
    }

    /**========================================================================================
     * setInitialCropWindowPaddingRatio - Начальное отступ окна обрезки от границ изображения в
     * процентах от размеров кадрируемого изображения.<br>
     * <i>По умолчанию: 0.1</i>
     */
    public ActivityBuilder setInitialCropWindowPaddingRatio(float initialCropWindowPaddingRatio) {
      mOptions.initialCropWindowPaddingRatio = initialCropWindowPaddingRatio;
      return this;
    }

    /**========================================================================================
     * setFixAspectRatio - следует ли сохранить соотношение ширины и высоты или изменить его.<br>
     * <i>По умолчанию: false</i>
     */
    public ActivityBuilder setFixAspectRatio(boolean fixAspectRatio) {
      mOptions.fixAspectRatio = fixAspectRatio;
      return this;
    }

    /**========================================================================================
     * setAspectRatio - значение соотношения сторон по осям X,Y.<br>
     * Также устанавливает для исправления соотношения сторон значение TRUE.<br>
     * <i>По умолчанию: 1/1</i>
     *
     * @param aspectRatioX ширина
     * @param aspectRatioY высота
     */
    public ActivityBuilder setAspectRatio(int aspectRatioX, int aspectRatioY) {
      mOptions.aspectRatioX = aspectRatioX;
      mOptions.aspectRatioY = aspectRatioY;
      mOptions.fixAspectRatio = true;
      return this;
    }

    /**========================================================================================
     * setBorderLineThickness - толщина направляющих линий (в пикселях).<br>
     * <i>По умолчанию: 3dp</i>
     */
    public ActivityBuilder setBorderLineThickness(float borderLineThickness) {
      mOptions.borderLineThickness = borderLineThickness;
      return this;
    }

    /**========================================================================================
     * setBorderLineColor - цвет направляющих линий.<br>
     * <i>По умолчанию: Color.argb(170, 255, 255, 255)</i>
     */
    public ActivityBuilder setBorderLineColor(int borderLineColor) {
      mOptions.borderLineColor = borderLineColor;
      return this;
    }

    /**========================================================================================
     * setBorderCornerThickness - толщина линии угла (в пикселях).<br>
     * <i>По умолчанию: 2dp</i>
     */
    public ActivityBuilder setBorderCornerThickness(float borderCornerThickness) {
      mOptions.borderCornerThickness = borderCornerThickness;
      return this;
    }

    /**========================================================================================
     * setBorderCornerOffset - смещение линии угла от границы окна обрезки (в пикселях).<br>
     * <i>По умолчанию: 5dp</i>
     */
    public ActivityBuilder setBorderCornerOffset(float borderCornerOffset) {
      mOptions.borderCornerOffset = borderCornerOffset;
      return this;
    }

    /**========================================================================================
     * setBorderCornerLength - длина линии угла от угла (в пикселях).<br>
     * <i>По умолчанию: 14dp</i>
     */
    public ActivityBuilder setBorderCornerLength(float borderCornerLength) {
      mOptions.borderCornerLength = borderCornerLength;
      return this;
    }

    /**========================================================================================
     * setBorderCornerColor - цвет линии угла.<br>
     * <i>По умолчанию: WHITE</i>
     */
    public ActivityBuilder setBorderCornerColor(int borderCornerColor) {
      mOptions.borderCornerColor = borderCornerColor;
      return this;
    }

    /**========================================================================================
     * setGuidelinesThickness - толщина направляющих линий (в пикселях).<br>
     * <i>По умолчанию: 1dp</i>
     */
    public ActivityBuilder setGuidelinesThickness(float guidelinesThickness) {
      mOptions.guidelinesThickness = guidelinesThickness;
      return this;
    }

    /**========================================================================================
     * setGuidelinesColor - цвет направляющих линий.<br>
     * <i>По умолчанию: Color.argb(170, 255, 255, 255)</i>
     */
    public ActivityBuilder setGuidelinesColor(int guidelinesColor) {
      mOptions.guidelinesColor = guidelinesColor;
      return this;
    }

    /**========================================================================================
     * setBackgroundColor - цвет фона наложения вокруг окна кадрирования покрывает части изображения,
     * не находящиеся в окне кадрирования.<br>
     * <i>По умолчанию: Color.argb(119, 0, 0, 0)</i>
     */
    public ActivityBuilder setBackgroundColor(int backgroundColor) {
      mOptions.backgroundColor = backgroundColor;
      return this;
    }

    /**========================================================================================
     * setMinCropWindowSize - минимальный размер окна кадрирования (в пикселях).<br>
     * <i>По умолчанию: 42dp, 42dp</i>
     */
    public ActivityBuilder setMinCropWindowSize(int minCropWindowWidth, int minCropWindowHeight) {
      mOptions.minCropWindowWidth = minCropWindowWidth;
      mOptions.minCropWindowHeight = minCropWindowHeight;
      return this;
    }

    /**========================================================================================
     * setMinCropResultSize - минимальный размер результирующего кадрируемого изображения
     * влияет на пределы окна кадрирования (в пикселях).<br>
     * <i>По умолчанию: 40px, 40px</i>
     */
    public ActivityBuilder setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
      mOptions.minCropResultWidth = minCropResultWidth;
      mOptions.minCropResultHeight = minCropResultHeight;
      return this;
    }

    /**========================================================================================
     * setMaxCropResultSize - максимальный размер результирующего кадрируемого изображения
     * влияет на пределы окна кадрирования (в пикселях).<br>
     * <i>По умолчанию: 99999, 99999</i>
     */
    public ActivityBuilder setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
      mOptions.maxCropResultWidth = maxCropResultWidth;
      mOptions.maxCropResultHeight = maxCropResultHeight;
      return this;
    }

    /**========================================================================================
     * setActivityTitle - название {@link CropImageActivity}.<br>
     * <i>По умолчанию: ""</i>
     */
    public ActivityBuilder setActivityTitle(CharSequence activityTitle) {
      mOptions.activityTitle = activityTitle;
      return this;
    }

    /**========================================================================================
     * setActivityMenuIconColor - цвет, используемый для значков элементов панели действий.<br>
     * <i>По умолчанию: NONE</i>
     */
    public ActivityBuilder setActivityMenuIconColor(int activityMenuIconColor) {
      mOptions.activityMenuIconColor = activityMenuIconColor;
      return this;
    }

    /**========================================================================================
     * setOutputUri - Android Uri для сохранения обрезанного изображения.<br>
     * <i>По умолчанию: NONE, создаст временный файл</i>
     */
    public ActivityBuilder setOutputUri(Uri outputUri) {
      mOptions.outputUri = outputUri;
      return this;
    }

    /**========================================================================================
     * setOutputCompressFormat - формат сжатия для использования при записи изображения.<be>
     * <i>По умолчанию: JPEG</i>
     */
    public ActivityBuilder setOutputCompressFormat(Bitmap.CompressFormat outputCompressFormat) {
      mOptions.outputCompressFormat = outputCompressFormat;
      return this;
    }

    /**========================================================================================
     * setOutputCompressQuality - качество (применимо) для использования при написании изображения (0–100).<br>
     * <i>По умолчанию: 90</i>
     */
    public ActivityBuilder setOutputCompressQuality(int outputCompressQuality) {
      mOptions.outputCompressQuality = outputCompressQuality;
      return this;
    }

    /**========================================================================================
     * setRequestedSize - размер, до которого нужно изменить размер обрезанного изображения.<br>
     * Использует параметр {@link CropImageView.RequestSizeOptions#RESIZE_INSIDE}.<br>
     * <i>По умолчанию: 0, 0 - не установлено, размер не изменится</i>
     */
    public ActivityBuilder setRequestedSize(int reqWidth, int reqHeight) {
      return setRequestedSize(reqWidth, reqHeight, CropImageView.RequestSizeOptions.RESIZE_INSIDE);
    }

    /**========================================================================================
     * setRequestedSize - размер, до которого нужно изменить размер обрезанного изображения.<br>
     * <i>По умолчанию: 0, 0 - не установлено, размер не изменится</i>
     */
    public ActivityBuilder setRequestedSize(
            int reqWidth, int reqHeight, CropImageView.RequestSizeOptions options) {
      mOptions.outputRequestWidth = reqWidth;
      mOptions.outputRequestHeight = reqHeight;
      mOptions.outputRequestSizeOptions = options;
      return this;
    }

    /**========================================================================================
     * setNoOutputImage - если результат действия по кадрированию изображения не должен
     * сохранять растровое изображение кадрированного изображения.<br>
     * Используется, если вы хотите обрезать изображение вручную и вам нужны только
     * прямоугольник обрезки и данные поворота.<br>
     * <i>По умолчанию: false</i>
     */
    public ActivityBuilder setNoOutputImage(boolean noOutputImage) {
      mOptions.noOutputImage = noOutputImage;
      return this;
    }

    /**========================================================================================
     * setInitialCropWindowRectangle - начальный прямоугольник, который будет установлен на
     * кадрируемом изображении после загрузки.<br>
     * <i>По умолчанию: NONE - будет инициализироваться с использованием начального
     * коэффициента заполнения окна обрезки</i>
     */
    public ActivityBuilder setInitialCropWindowRectangle(Rect initialCropWindowRectangle) {
      mOptions.initialCropWindowRectangle = initialCropWindowRectangle;
      return this;
    }

    /**========================================================================================
     * setInitialRotation - начальный поворот, который нужно установить на кадрируемом изображении
     * после загрузки (0-360 градусов по часовой стрелке). <br>
     * <i>По умолчанию: NONE - будет читать данные exif изображения</i>
     */
    public ActivityBuilder setInitialRotation(int initialRotation) {
      mOptions.initialRotation = (initialRotation + 360) % 360;
      return this;
    }

    /**========================================================================================
     * setAllowRotation - если разрешить вращение во время обрезки.<br>
     * <i>По умолчанию: true</i>
     */
    public ActivityBuilder setAllowRotation(boolean allowRotation) {
      mOptions.allowRotation = allowRotation;
      return this;
    }

    /**========================================================================================
     * setAllowFlipping - если разрешить переворачивание во время кадрирования.<br>
     * <i>Default: true</i>
     */
    public ActivityBuilder setAllowFlipping(boolean allowFlipping) {
      mOptions.allowFlipping = allowFlipping;
      return this;
    }

    /**========================================================================================
     * setAllowCounterRotation - если разрешить вращение против часовой стрелки во время кадрирования.<br>
     * Примечание. Если вращение отключено, этот параметр не действует.<br>
     * <i>По умолчанию: false</i>
     */
    public ActivityBuilder setAllowCounterRotation(boolean allowCounterRotation) {
      mOptions.allowCounterRotation = allowCounterRotation;
      return this;
    }

    /**========================================================================================
     * setRotationDegrees - Количество градусов для поворота по часовой стрелке или против
     * часовой стрелки (0-360).<br>
     * <i>По умолчанию: 90</i>
     */
    public ActivityBuilder setRotationDegrees(int rotationDegrees) {
      mOptions.rotationDegrees = (rotationDegrees + 360) % 360;
      return this;
    }

    /**========================================================================================
     * setFlipHorizontally - следует ли переворачивать изображение по горизонтали.<br>
     * <i>По умолчанию: false</i>
     */
    public ActivityBuilder setFlipHorizontally(boolean flipHorizontally) {
      mOptions.flipHorizontally = flipHorizontally;
      return this;
    }

    /**========================================================================================
     * setFlipVertically - следует ли переворачивать изображение по вертикали.<br>
     * <i>По умолчанию: false</i>
     */
    public ActivityBuilder setFlipVertically(boolean flipVertically) {
      mOptions.flipVertically = flipVertically;
      return this;
    }

    /**========================================================================================
     * setCropMenuCropButtonTitle - необязательно, установите заголовок кнопки обрезки в меню обрезки.<br>
     * <i>По умолчанию: null, будет использоваться строка ресурса:crop_image_menu_crop</i>
     */
    public ActivityBuilder setCropMenuCropButtonTitle(CharSequence title) {
      mOptions.cropMenuCropButtonTitle = title;
      return this;
    }

    /**========================================================================================
     * setCropMenuCropButtonIcon - Идентификатор ресурса изображения, который будет
     * использоваться для значка обрезки вместо текста.<br>
     * <i>По умолчанию: 0</i>
     */
    public ActivityBuilder setCropMenuCropButtonIcon(@DrawableRes int drawableResource) {
      mOptions.cropMenuCropButtonIcon = drawableResource;
      return this;
    }
  }

  // endregion

  //=============================================================================
  // region: Внутренний класс: ActivityResult
  //=============================================================================

  /** ActivityResult - Данные о результатах действия Crop Image Activity. */
  public static final class ActivityResult extends CropImageView.CropResult implements Parcelable {

    // общедоступный статический интерфейс Parcelable.Creator
    // Интерфейс, который должен быть реализован и предоставлен как общедоступное поле CREATOR,
    // которое генерирует экземпляры вашего класса Parcelable из Parcel.
    public static final Creator<ActivityResult> CREATOR =
            new Creator<ActivityResult>() {
              @Override
              public ActivityResult createFromParcel(Parcel in) {
                return new ActivityResult(in);
              }

              @Override
              public ActivityResult[] newArray(int size) {
                return new ActivityResult[size];
              }
            };

    /**================================================================================
     * ActivityResult - конструктор
     *
     * @param originalUri
     * @param uri
     * @param error
     * @param cropPoints
     * @param cropRect
     * @param rotation
     * @param wholeImageRect
     * @param sampleSize
     */
    public ActivityResult(
            Uri originalUri,
            Uri uri,
            Exception error,
            float[] cropPoints,
            Rect cropRect,
            int rotation,
            Rect wholeImageRect,
            int sampleSize) {
      super(
              null,
              originalUri,
              null,
              uri,
              error,
              cropPoints,
              cropRect,
              wholeImageRect,
              rotation,
              sampleSize);
    }

    /**================================================================================
     * ActivityResult - конструктор
     *
     * @param in
     */
    protected ActivityResult(Parcel in) {
      super(
              null,
              (Uri) in.readParcelable(Uri.class.getClassLoader()),
              null,
              (Uri) in.readParcelable(Uri.class.getClassLoader()),
              (Exception) in.readSerializable(),
              in.createFloatArray(),
              (Rect) in.readParcelable(Rect.class.getClassLoader()),
              (Rect) in.readParcelable(Rect.class.getClassLoader()),
              in.readInt(),
              in.readInt());
    }

    /**================================================================================
     * writeToParcel - Сведите этот объект в Parcel.
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelable(getOriginalUri(), flags);
      dest.writeParcelable(getUri(), flags);
      dest.writeSerializable(getError());
      dest.writeFloatArray(getCropPoints());
      dest.writeParcelable(getCropRect(), flags);
      dest.writeParcelable(getWholeImageRect(), flags);
      dest.writeInt(getRotation());
      dest.writeInt(getSampleSize());
    }

    /**================================================================================
     * describeContents - Опишите виды специальных объектов, содержащихся в маршалированном
     * представлении этого экземпляра Parcelable. Например, если объект будет включать дескриптор
     * файла в выходные данные writeToParcel(android.os.Parcel, int), возвращаемое значение этого
     * метода должно включать бит CONTENTS_FILE_DESCRIPTOR.
     *
     * @return
     */
    @Override
    public int describeContents() {
      return 0;
    }
  }

  // endregion

}

