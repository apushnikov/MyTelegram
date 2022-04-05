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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

//import com.apushnikov.other361_sample_image_cropper.R;

import com.apushnikov.other351_mytelegram.R;

import java.io.File;
import java.io.IOException;


//TODO: Перенести в 362 строковый ресурс crop_image_activity_no_permissions!!!


/**===========================================================================================
 * Встроенная активность для обрезки изображений.<br>
 * Используйте {@link CropImage#activity(Uri)}, чтобы создать построитель для запуска этого действия.<br>
 * Use {@link CropImage#activity(Uri)} to create a builder to start this activity.
 */
public class CropImageActivity extends AppCompatActivity
    implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

  //===========================================================================================
  // region: Поля и константы
  //===========================================================================================

  /** Виджет библиотеки изображений обрезки, используемый в действии */
  private CropImageView mCropImageView;

  /**Сохраненное URI-изображение, предназначенное для обрезки URI, если требуются определенные разрешения<br>
   * Persist URI image to crop URI if specific permissions are required */
  private Uri mCropImageUri;

  /** параметры, которые были установлены для обрезки изображения */
  private CropImageOptions mOptions;

  // endregion

  //===========================================================================================
  // region: Методы
  //===========================================================================================


  /**===========================================================================================
   * onCreate
   *
   * @param savedInstanceState Bundle, содержащий ранее сохраненное состояние действия
   */
  @Override
  @SuppressLint("NewApi")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.crop_image_activity);

    mCropImageView = findViewById(R.id.cropImageView);

    // Извлечение параметров из интента, с помощью которого вызвана это Activity
    // Извлекаем bundle из интента
    Bundle bundle = getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
    // Извлекаем из bundle сохраненное URI-изображение, предназначенное для обрезки URI
    mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE);
    // Извлекаем из bundle параметры, которые были установлены для обрезки изображения
    mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

    // Если ранее сохраненное состояние = null (т.е. активность запускается впервые)
    if (savedInstanceState == null) {
      // Если переданное Сохраненное URI-изображение = null ИЛИ пусто
      if (mCropImageUri == null || mCropImageUri.equals(Uri.EMPTY)) {
        // Если требуется ли явный запрос разрешения камеры
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
          // запрашиваем разрешения CAMERA и обрабатываем результат в onRequestPermissionsResult()
          requestPermissions(
              new String[] {Manifest.permission.CAMERA},
              CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        }
        // Если НЕ требуется ли явный запрос разрешения камеры
        else {
          // Запускаем активити, чтобы получить изображение для обрезки с помощью средства выбора,
          // которое будет иметь все доступные приложения для устройства, такие как
          //    - камера (MyCamera),
          //    - галерея (Photos),
          //    - магазин приложений (Dropbox).
          CropImage.startPickImageActivity(this);
        }
      }
      // Если переданное Сохраненное URI-изображение НЕ null И НЕ пусто
      else {
        // Проверьте, требуются ли для выбранного URI изображения разрешения READ_EXTERNAL_STORAGE.
        // true — требуемые разрешения не предоставляются
        if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
          // запрашиваем разрешения READ_EXTERNAL_STORAGE и обрабатываем результат в onRequestPermissionsResult()
          requestPermissions(
                  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                  CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
        }
        // false — либо разрешения не требуются, либо они даны
        else {
          // разрешения не требуются или уже получены, можно начать обрезать изображение
          mCropImageView.setImageUriAsync(mCropImageUri);
        }

      }
    }

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      CharSequence title = mOptions != null &&
          mOptions.activityTitle != null && mOptions.activityTitle.length() > 0
              ? mOptions.activityTitle
              : getResources().getString(R.string.crop_image_activity_title);
      actionBar.setTitle(title);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**===========================================================================================
   * onStart
   */
  @Override
  protected void onStart() {
    super.onStart();
    mCropImageView.setOnSetImageUriCompleteListener(this);
    mCropImageView.setOnCropImageCompleteListener(this);
  }

  /**===========================================================================================
   * onStop
   */
  @Override
  protected void onStop() {
    super.onStop();
    mCropImageView.setOnSetImageUriCompleteListener(null);
    mCropImageView.setOnCropImageCompleteListener(null);
  }

  /**===========================================================================================
   * onCreateOptionsMenu - создание меню
   *
   * @param menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.crop_image_menu, menu);

    if (!mOptions.allowRotation) {
      menu.removeItem(R.id.crop_image_menu_rotate_left);
      menu.removeItem(R.id.crop_image_menu_rotate_right);
    } else if (mOptions.allowCounterRotation) {
      menu.findItem(R.id.crop_image_menu_rotate_left).setVisible(true);
    }

    if (!mOptions.allowFlipping) {
      menu.removeItem(R.id.crop_image_menu_flip);
    }

    if (mOptions.cropMenuCropButtonTitle != null) {
      menu.findItem(R.id.crop_image_menu_crop).setTitle(mOptions.cropMenuCropButtonTitle);
    }

    Drawable cropIcon = null;
    try {
      if (mOptions.cropMenuCropButtonIcon != 0) {
        cropIcon = ContextCompat.getDrawable(this, mOptions.cropMenuCropButtonIcon);
        menu.findItem(R.id.crop_image_menu_crop).setIcon(cropIcon);
      }
    } catch (Exception e) {
      Log.w("AIC", "Failed to read menu crop drawable", e);
    }

    if (mOptions.activityMenuIconColor != 0) {
      updateMenuItemIconColor(
          menu, R.id.crop_image_menu_rotate_left, mOptions.activityMenuIconColor);
      updateMenuItemIconColor(
          menu, R.id.crop_image_menu_rotate_right, mOptions.activityMenuIconColor);
      updateMenuItemIconColor(menu, R.id.crop_image_menu_flip, mOptions.activityMenuIconColor);
      if (cropIcon != null) {
        updateMenuItemIconColor(menu, R.id.crop_image_menu_crop, mOptions.activityMenuIconColor);
      }
    }
    return true;
  }

  /**===========================================================================================
   * onOptionsItemSelected - реакция на нажатия меню
   *
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.crop_image_menu_crop) {
      cropImage();
      return true;
    }
    if (item.getItemId() == R.id.crop_image_menu_rotate_left) {
      rotateImage(-mOptions.rotationDegrees);
      return true;
    }
    if (item.getItemId() == R.id.crop_image_menu_rotate_right) {
      rotateImage(mOptions.rotationDegrees);
      return true;
    }
    if (item.getItemId() == R.id.crop_image_menu_flip_horizontally) {
      mCropImageView.flipImageHorizontally();
      return true;
    }
    if (item.getItemId() == R.id.crop_image_menu_flip_vertically) {
      mCropImageView.flipImageVertically();
      return true;
    }
    if (item.getItemId() == android.R.id.home) {
      setResultCancel();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**===========================================================================================
   * onBackPressed - нажата кнопка назад
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    setResultCancel();
  }

  /**===========================================================================================
   * onActivityResult
   *
   * @param requestCode
   * @param resultCode
   * @param data
   */
  @Override
  @SuppressLint("NewApi")
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // handle result of pick image chooser
    // обрабатывать результат выбора изображения
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_CANCELED) {
        // User cancelled the picker. We don't have anything to crop
        // Пользователь отменил сборщик. Нам нечего обрезать

        // TODO: При запуске фото мы попадаем сюда (это не правильно)
        //  - Пользователь отменил сборщик. Нам нечего обрезать
        setResultCancel();
      }

      if (resultCode == Activity.RESULT_OK) {
        mCropImageUri = CropImage.getPickImageResultUri(this, data);

        // Для API >= 23 нам нужно специально проверить, есть ли у нас права на чтение
        // внешнего хранилища.
        if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {

          // запрашиваем разрешения READ_EXTERNAL_STORAGE и обрабатываем результат в onRequestPermissionsResult()
          requestPermissions(
                  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                  CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
        } else {
          // разрешения не требуются или уже получены, можно начать обрезать изображение

          // TODO: При запуске галереи мы попадаем сюда (это правильно)
          //  - Запускаем mCropImageView.setImageUriAsync(mCropImageUri)
          mCropImageView.setImageUriAsync(mCropImageUri);
        }
      }
    }
  }

  /**===========================================================================================
   * onRequestPermissionsResult - обработка полученных разрешений
   *
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // Если запросили разрешения READ_EXTERNAL_STORAGE
    if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
      // Если Сохраненное URI-изображение, предназначенное для обрезки URI НЕ null
      // И разрешение READ_EXTERNAL_STORAGE предоставлено
      if (mCropImageUri != null
              && grantResults.length > 0
              && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // необходимые разрешения предоставлены, начните обрезать изображение
        mCropImageView.setImageUriAsync(mCropImageUri);
      }
      // Если Сохраненное URI-изображение, предназначенное для обрезки URI равно null
      // ИЛИ разрешение READ_EXTERNAL_STORAGE НЕ предоставлено
      else {
        Toast.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG).show();
        // Отмена обрезки
        setResultCancel();
      }
    }

    // Если запросили разрешения CAMERA
    if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
      // Независимо от того, было дано разрешение на камеру или нет, мы показываем пикер
      // Средство выбора не добавит намерение камеры, если разрешение недоступно.

      // Запускаем активити, чтобы получить изображение для обрезки с помощью средства выбора,
      // которое будет иметь все доступные приложения для устройства, такие как
      //    - камера (MyCamera),
      //    - галерея (Photos),
      //    - магазин приложений (Dropbox).
      CropImage.startPickImageActivity(this);
    }
  }

  /**===========================================================================================
   * onSetImageUriComplete
   *
   * @param view The crop image view that loading of image was complete.
   * @param uri the URI of the image that was loading
   * @param error if error occurred during loading will contain the error, otherwise null.
   */
  @Override
  public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
    if (error == null) {
      if (mOptions.initialCropWindowRectangle != null) {
        mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
      }
      if (mOptions.initialRotation > -1) {
        mCropImageView.setRotatedDegrees(mOptions.initialRotation);
      }
    } else {
      setResult(null, error, 1);
    }
  }

  /**===========================================================================================
   * onCropImageComplete
   *
   * @param view The crop image view that cropping of image was complete.
   * @param result the crop image result data (with cropped image or error)
   */
  @Override
  public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
    setResult(result.getUri(), result.getError(), result.getSampleSize());
  }

  // endregion

  //===========================================================================================
  // region: Частные методы
  //===========================================================================================

  /**===========================================================================================
   * cropImage - Выполните обрезку изображения и сохраните результат для вывода URI.
   */
  protected void cropImage() {
    if (mOptions.noOutputImage) {
      setResult(null, null, 1);
    } else {
      Uri outputUri = getOutputUri();
      mCropImageView.saveCroppedImageAsync(
          outputUri,
          mOptions.outputCompressFormat,
          mOptions.outputCompressQuality,
          mOptions.outputRequestWidth,
          mOptions.outputRequestHeight,
          mOptions.outputRequestSizeOptions);
    }
  }

  /**===========================================================================================
   * rotateImage - Поверните изображение в режиме кадрирования изображения.
   */
  protected void rotateImage(int degrees) {
    mCropImageView.rotateImage(degrees);
  }

  /**===========================================================================================
   * getOutputUri - Получите uri Android, чтобы сохранить обрезанное изображение.<br>
   * Используйте указанные параметры или создайте временный файл.
   */
  protected Uri getOutputUri() {
    Uri outputUri = mOptions.outputUri;
    if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
      try {
        String ext =
            mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                ? ".jpg"
                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
        outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
      } catch (IOException e) {
        throw new RuntimeException("Failed to create temp file for output image", e);
      }
    }
    return outputUri;
  }

  /**===========================================================================================
   * setResult - Результат с обрезанными данными изображения или ошибка в случае неудачи.
   */
  protected void setResult(Uri uri, Exception error, int sampleSize) {
    int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
    setResult(resultCode, getResultIntent(uri, error, sampleSize));
    finish();
  }

  /**===========================================================================================
   * setResultCancel - Отмена обрезки.
   */
  protected void setResultCancel() {
    setResult(RESULT_CANCELED);
    finish();
  }

  /**===========================================================================================
   * getResultIntent - Получите экземпляр намерения, который будет использоваться для результата
   * этого действия.
   */
  protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
    CropImage.ActivityResult result =
        new CropImage.ActivityResult(
            mCropImageView.getImageUri(),
            uri,
            error,
            mCropImageView.getCropPoints(),
            mCropImageView.getCropRect(),
            mCropImageView.getRotatedDegrees(),
            mCropImageView.getWholeImageRect(),
            sampleSize);
    Intent intent = new Intent();
    intent.putExtras(getIntent());
    intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
    return intent;
  }

  /**===========================================================================================
   * updateMenuItemIconColor - Обновите цвет определенного пункта меню до заданного цвета.
   */
  private void updateMenuItemIconColor(Menu menu, int itemId, int color) {
    MenuItem menuItem = menu.findItem(itemId);
    if (menuItem != null) {
      Drawable menuItemIcon = menuItem.getIcon();
      if (menuItemIcon != null) {
        try {
          menuItemIcon.mutate();
          menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
          menuItem.setIcon(menuItemIcon);
        } catch (Exception e) {
          Log.w("AIC", "Failed to update menu item color", e);
        }
      }
    }
  }

  // endregion

}
