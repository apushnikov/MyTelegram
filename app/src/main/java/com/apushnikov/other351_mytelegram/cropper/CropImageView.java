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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.exifinterface.media.ExifInterface;

//import com.apushnikov.other361_sample_image_cropper.R;

import com.apushnikov.other351_mytelegram.R;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**=========================================================================================
 * CropImageView - Пользовательский вид, предоставляющий возможности кадрирования изображения. */
public class CropImageView extends FrameLayout {

  //===========================================================================================
  // region: Поля и константы
  //===========================================================================================

  /** mImageView - Виджет просмотра изображения, используемый для показа изображения для обрезки. */
  private final ImageView mImageView;

  /** mCropOverlayView - Наложение поверх представления изображения, чтобы показать
   * пользовательский интерфейс обрезки. */
  private final CropOverlayView mCropOverlayView;

  /** mImageMatrix - Матрица, используемая для преобразования обрезаемого изображения
   * в представление изображения. */
  private final Matrix mImageMatrix = new Matrix();

  /** mImageInverseMatrix - Повторное использование экземпляра матрицы для вычислений обратной матрицы. */
  private final Matrix mImageInverseMatrix = new Matrix();

  /** mProgressBar - Виджет индикатора выполнения для отображения индикатора выполнения
   * при асинхронной загрузке и обрезке изображения. */
  private final ProgressBar mProgressBar;

  /** mImagePoints - Прямоугольник, используемый при вычислении преобразования матрицы изображения
   * (повторное использование экземпляра прямоугольника) */
  private final float[] mImagePoints = new float[8];

  /** mScaleImagePoints - Прямоугольник, используемый в преобразовании матрицы изображения
   * для вычисления масштаба (повторное использование экземпляра прямоугольника) */
  private final float[] mScaleImagePoints = new float[8];

  /** mAnimation - Класс анимации для плавного увеличения/уменьшения анимации */
  private CropImageAnimation mAnimation;

  /** mBitmap - битмап */
  private Bitmap mBitmap;

  /** mInitialDegreesRotated - Значение поворота изображения, используемое во время загрузки
   * изображения, поэтому мы можем сбросить его. */
  private int mInitialDegreesRotated;

  /** mDegreesRotated - Насколько изображение повернуто от оригинала по часовой стрелке */
  private int mDegreesRotated;

  /** mFlipHorizontally - если изображение перевернуто по горизонтали */
  private boolean mFlipHorizontally;

  /** mFlipVertically - если изображение перевернуто по вертикали */
  private boolean mFlipVertically;

  private int mLayoutWidth;

  private int mLayoutHeight;

  private int mImageResource;

  /** mScaleType - Тип начального масштаба изображения в представлении кадрированного изображения */
  private ScaleType mScaleType;

  /** mSaveBitmapToInstanceState - если сохранить растровое изображение при сохранении состояния экземпляра.<br>
   * Лучше всего этого избежать, используя URI в настройках изображения для обрезки.<br>
   * Если false, растровое изображение не сохраняется, и если для просмотра требуется
   * восстановление, оно будет пустым, сохранение растрового изображения требует сохранения
   * его в файл, что может быть дорогостоящим. по умолчанию: false.
   */
  private boolean mSaveBitmapToInstanceState = false;

  /** mShowCropOverlay - если показать пользовательский интерфейс наложения обрезки, который
   * содержит пользовательский интерфейс окна обрезки, окруженный фоном поверх изображения обрезки.<br>
   * по умолчанию: true, можно отключить для анимации или смены кадров.
   */
  private boolean mShowCropOverlay = true;

  /** mShowProgressBar - если отображать индикатор выполнения, когда выполняется асинхронная
   * загрузка/обрезка изображения.<br>
   * по умолчанию: true, отключите, чтобы предоставить пользовательский интерфейс индикатора выполнения.
   */
  private boolean mShowProgressBar = true;

  /** mAutoZoomEnabled - если функция автоматического масштабирования включена.<br>
   * по умолчанию: true.
   */
  private boolean mAutoZoomEnabled = true;

  /** mMaxZoom - Максимальный зум, разрешенный во время обрезки */
  private int mMaxZoom;

  /** mOnCropOverlayReleasedListener - обратный вызов, который будет вызываться при освобождении
   * наложения кадрирования. */
  private OnSetCropOverlayReleasedListener mOnCropOverlayReleasedListener;

  /** mOnSetCropOverlayMovedListener - обратный вызов, который будет вызываться при перемещении
   * наложения обрезки. */
  private OnSetCropOverlayMovedListener mOnSetCropOverlayMovedListener;

  /** mOnSetCropWindowChangeListener - обратный вызов, который будет вызываться при изменении окна обрезки. */
  private OnSetCropWindowChangeListener mOnSetCropWindowChangeListener;

  /** mOnSetImageUriCompleteListener - обратный вызов, который будет вызван после завершения
   * асинхронной загрузки изображения. */
  private OnSetImageUriCompleteListener mOnSetImageUriCompleteListener;

  /** mOnCropImageCompleteListener - обратный вызов, который будет вызван после завершения
   * асинхронной обрезки изображения. */
  private OnCropImageCompleteListener mOnCropImageCompleteListener;

  /** mLoadedImageUri - URI, с которого было загружено изображение (если загружено с URI) */
  private Uri mLoadedImageUri;

  /** mLoadedSampleSize - Размер образца, по которому изображение было загружено,
   * если оно было загружено по URI. */
  private int mLoadedSampleSize = 1;

  /** Текущий уровень масштабирования для масштабирования кадрируемого изображения. */
  private float mZoom = 1;

  /** Смещение по оси X, на которое обрезаемое изображение было перемещено после масштабирования. */
  private float mZoomOffsetX;

  /** Смещение по оси Y, на которое обрезаемое изображение было перемещено после масштабирования. */
  private float mZoomOffsetY;

  /** Используется для восстановления прямоугольника обрезки окон после восстановления состояния */
  private RectF mRestoreCropWindowRect;

  /** Используется для восстановления поворота изображения после восстановления состояния */
  private int mRestoreDegreesRotated;

  /** Используется для обнаружения изменения размера для обработки автоматического масштабирования
   * с помощью {@link #handleCropWindowChanged(boolean, boolean)} в {@link #layout(int, int, int, int)}.
   */
  private boolean mSizeChanged;

  /** Временный URI, используемый для сохранения растрового изображения на диск, чтобы сохранить,
   * например, состояние экземпляра в случае, если обрезка была установлена с растровым изображением.
   */
  private Uri mSaveInstanceStateBitmapUri;

  /** Задача, используемая для асинхронной загрузки растрового изображения из потока
   * пользовательского интерфейса. */
  private WeakReference<BitmapLoadingWorkerTask> mBitmapLoadingWorkerTask;

  /** Задача, используемая для асинхронной обрезки растрового изображения из потока
   * пользовательского интерфейса. */
  private WeakReference<BitmapCroppingWorkerTask> mBitmapCroppingWorkerTask;

  // endregion

  //===========================================================================================
  // region: Методы
  //===========================================================================================

  /**========================================================================================
   * CropImageView - конструктор
   *
   * @param context
   */
  public CropImageView(Context context) {
    this(context, null);
  }

  /**========================================================================================
   * CropImageView - конструктор
   *
   * @param context
   * @param attrs
   */
  public CropImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

    CropImageOptions options = null;
    Intent intent = context instanceof Activity ? ((Activity) context).getIntent() : null;
    if (intent != null) {
      Bundle bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
      if (bundle != null) {
        options = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);
      }
    }

    if (options == null) {

      options = new CropImageOptions();

      if (attrs != null) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);
        try {
          options.fixAspectRatio =
                  ta.getBoolean(R.styleable.CropImageView_cropFixAspectRatio, options.fixAspectRatio);
          options.aspectRatioX =
                  ta.getInteger(R.styleable.CropImageView_cropAspectRatioX, options.aspectRatioX);
          options.aspectRatioY =
                  ta.getInteger(R.styleable.CropImageView_cropAspectRatioY, options.aspectRatioY);
          options.scaleType =
                  ScaleType.values()[
                          ta.getInt(R.styleable.CropImageView_cropScaleType, options.scaleType.ordinal())];
          options.autoZoomEnabled =
                  ta.getBoolean(R.styleable.CropImageView_cropAutoZoomEnabled, options.autoZoomEnabled);
          options.multiTouchEnabled =
                  ta.getBoolean(
                          R.styleable.CropImageView_cropMultiTouchEnabled, options.multiTouchEnabled);
          options.maxZoom = ta.getInteger(R.styleable.CropImageView_cropMaxZoom, options.maxZoom);
          options.cropShape =
                  CropShape.values()[
                          ta.getInt(R.styleable.CropImageView_cropShape, options.cropShape.ordinal())];
          options.guidelines =
                  Guidelines.values()[
                          ta.getInt(
                                  R.styleable.CropImageView_cropGuidelines, options.guidelines.ordinal())];
          options.snapRadius =
                  ta.getDimension(R.styleable.CropImageView_cropSnapRadius, options.snapRadius);
          options.touchRadius =
                  ta.getDimension(R.styleable.CropImageView_cropTouchRadius, options.touchRadius);
          options.initialCropWindowPaddingRatio =
                  ta.getFloat(
                          R.styleable.CropImageView_cropInitialCropWindowPaddingRatio,
                          options.initialCropWindowPaddingRatio);
          options.borderLineThickness =
                  ta.getDimension(
                          R.styleable.CropImageView_cropBorderLineThickness, options.borderLineThickness);
          options.borderLineColor =
                  ta.getInteger(R.styleable.CropImageView_cropBorderLineColor, options.borderLineColor);
          options.borderCornerThickness =
                  ta.getDimension(
                          R.styleable.CropImageView_cropBorderCornerThickness,
                          options.borderCornerThickness);
          options.borderCornerOffset =
                  ta.getDimension(
                          R.styleable.CropImageView_cropBorderCornerOffset, options.borderCornerOffset);
          options.borderCornerLength =
                  ta.getDimension(
                          R.styleable.CropImageView_cropBorderCornerLength, options.borderCornerLength);
          options.borderCornerColor =
                  ta.getInteger(
                          R.styleable.CropImageView_cropBorderCornerColor, options.borderCornerColor);
          options.guidelinesThickness =
                  ta.getDimension(
                          R.styleable.CropImageView_cropGuidelinesThickness, options.guidelinesThickness);
          options.guidelinesColor =
                  ta.getInteger(R.styleable.CropImageView_cropGuidelinesColor, options.guidelinesColor);
          options.backgroundColor =
                  ta.getInteger(R.styleable.CropImageView_cropBackgroundColor, options.backgroundColor);
          options.showCropOverlay =
                  ta.getBoolean(R.styleable.CropImageView_cropShowCropOverlay, mShowCropOverlay);
          options.showProgressBar =
                  ta.getBoolean(R.styleable.CropImageView_cropShowProgressBar, mShowProgressBar);
          options.borderCornerThickness =
                  ta.getDimension(
                          R.styleable.CropImageView_cropBorderCornerThickness,
                          options.borderCornerThickness);
          options.minCropWindowWidth =
                  (int)
                          ta.getDimension(
                                  R.styleable.CropImageView_cropMinCropWindowWidth, options.minCropWindowWidth);
          options.minCropWindowHeight =
                  (int)
                          ta.getDimension(
                                  R.styleable.CropImageView_cropMinCropWindowHeight,
                                  options.minCropWindowHeight);
          options.minCropResultWidth =
                  (int)
                          ta.getFloat(
                                  R.styleable.CropImageView_cropMinCropResultWidthPX,
                                  options.minCropResultWidth);
          options.minCropResultHeight =
                  (int)
                          ta.getFloat(
                                  R.styleable.CropImageView_cropMinCropResultHeightPX,
                                  options.minCropResultHeight);
          options.maxCropResultWidth =
                  (int)
                          ta.getFloat(
                                  R.styleable.CropImageView_cropMaxCropResultWidthPX,
                                  options.maxCropResultWidth);
          options.maxCropResultHeight =
                  (int)
                          ta.getFloat(
                                  R.styleable.CropImageView_cropMaxCropResultHeightPX,
                                  options.maxCropResultHeight);
          options.flipHorizontally =
                  ta.getBoolean(
                          R.styleable.CropImageView_cropFlipHorizontally, options.flipHorizontally);
          options.flipVertically =
                  ta.getBoolean(R.styleable.CropImageView_cropFlipHorizontally, options.flipVertically);

          mSaveBitmapToInstanceState =
                  ta.getBoolean(
                          R.styleable.CropImageView_cropSaveBitmapToInstanceState,
                          mSaveBitmapToInstanceState);

          // if aspect ratio is set then set fixed to true
          if (ta.hasValue(R.styleable.CropImageView_cropAspectRatioX)
                  && ta.hasValue(R.styleable.CropImageView_cropAspectRatioX)
                  && !ta.hasValue(R.styleable.CropImageView_cropFixAspectRatio)) {
            options.fixAspectRatio = true;
          }
        } finally {
          ta.recycle();
        }
      }
    }

    options.validate();

    mScaleType = options.scaleType;
    mAutoZoomEnabled = options.autoZoomEnabled;
    mMaxZoom = options.maxZoom;
    mShowCropOverlay = options.showCropOverlay;
    mShowProgressBar = options.showProgressBar;
    mFlipHorizontally = options.flipHorizontally;
    mFlipVertically = options.flipVertically;

    LayoutInflater inflater = LayoutInflater.from(context);
    View v = inflater.inflate(R.layout.crop_image_view, this, true);

    mImageView = v.findViewById(R.id.ImageView_image);
    mImageView.setScaleType(ImageView.ScaleType.MATRIX);

    mCropOverlayView = v.findViewById(R.id.CropOverlayView);
    mCropOverlayView.setCropWindowChangeListener(
            new CropOverlayView.CropWindowChangeListener() {
              @Override
              public void onCropWindowChanged(boolean inProgress) {
                handleCropWindowChanged(inProgress, true);
                OnSetCropOverlayReleasedListener listener = mOnCropOverlayReleasedListener;
                if (listener != null && !inProgress) {
                  listener.onCropOverlayReleased(getCropRect());
                }
                OnSetCropOverlayMovedListener movedListener = mOnSetCropOverlayMovedListener;
                if (movedListener != null && inProgress) {
                  movedListener.onCropOverlayMoved(getCropRect());
                }
              }
            });
    mCropOverlayView.setInitialAttributeValues(options);

    mProgressBar = v.findViewById(R.id.CropProgressBar);
    setProgressBarVisibility();
  }

  /**==========================================================================================
   * Получите тип масштаба изображения в представлении обрезки. */
  public ScaleType getScaleType() {
    return mScaleType;
  }

  /**==========================================================================================
   * Установите тип масштаба изображения в режиме обрезки */
  public void setScaleType(ScaleType scaleType) {
    if (scaleType != mScaleType) {
      mScaleType = scaleType;
      mZoom = 1;
      mZoomOffsetX = mZoomOffsetY = 0;
      mCropOverlayView.resetCropOverlayView();
      requestLayout();
    }
  }

  /**==========================================================================================
   * Форма области кадрирования - прямоугольник/круг. */
  public CropShape getCropShape() {
    return mCropOverlayView.getCropShape();
  }

  /**==========================================================================================
   * Форма области кадрирования - прямоугольник/круг.<br>
   * Чтобы установить форму кадра квадрат/круг, установите соотношение сторон 1:1.
   */
  public void setCropShape(CropShape cropShape) {
    mCropOverlayView.setCropShape(cropShape);
  }

  /**==========================================================================================
   * если функция автоматического масштабирования включена. по умолчанию: правда. */
  public boolean isAutoZoomEnabled() {
    return mAutoZoomEnabled;
  }

  /**==========================================================================================
   * Включите/отключите функцию автоматического масштабирования. */
  public void setAutoZoomEnabled(boolean autoZoomEnabled) {
    if (mAutoZoomEnabled != autoZoomEnabled) {
      mAutoZoomEnabled = autoZoomEnabled;
      handleCropWindowChanged(false, false);
      mCropOverlayView.invalidate();
    }
  }

  /**==========================================================================================
   * Включите/отключите функцию мультитач. */
  public void setMultiTouchEnabled(boolean multiTouchEnabled) {
    if (mCropOverlayView.setMultiTouchEnabled(multiTouchEnabled)) {
      handleCropWindowChanged(false, false);
      mCropOverlayView.invalidate();
    }
  }

  /**==========================================================================================
   * Максимальное увеличение, разрешенное во время обрезки. */
  public int getMaxZoom() {
    return mMaxZoom;
  }

  /**==========================================================================================
   * Максимальное увеличение, разрешенное во время обрезки. */
  public void setMaxZoom(int maxZoom) {
    if (mMaxZoom != maxZoom && maxZoom > 0) {
      mMaxZoom = maxZoom;
      handleCropWindowChanged(false, false);
      mCropOverlayView.invalidate();
    }
  }

  /**==========================================================================================
   * минимальный размер результирующего кадрируемого изображения влияет на пределы окна
   * кадрирования (в пикселях).<br>
   */
  public void setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
    mCropOverlayView.setMinCropResultSize(minCropResultWidth, minCropResultHeight);
  }

  /**==========================================================================================
   * Максимальный размер результирующего кадрируемого изображения влияет на пределы окна
   * кадрирования (в пикселях).<br>
   */
  public void setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
    mCropOverlayView.setMaxCropResultSize(maxCropResultWidth, maxCropResultHeight);
  }

  /**==========================================================================================
   * Получите количество градусов, на которое кадрированное изображение повернуто по часовой стрелке.<br>
   *
   * @return 0-360
   */
  public int getRotatedDegrees() {
    return mDegreesRotated;
  }

  /**==========================================================================================
   * Задайте угол поворота кадрируемого изображения по часовой стрелке.<br>
   *
   * @param degrees 0-360
   */
  public void setRotatedDegrees(int degrees) {
    if (mDegreesRotated != degrees) {
      rotateImage(degrees - mDegreesRotated);
    }
  }

  /**==========================================================================================
   * фиксировано ли соотношение сторон или нет; true фиксирует соотношение сторон,
   * а false позволяет его изменить.
   */
  public boolean isFixAspectRatio() {
    return mCropOverlayView.isFixAspectRatio();
  }

  /**==========================================================================================
   * Устанавливает, является ли соотношение сторон фиксированным или нет; true фиксирует
   * соотношение сторон, а false позволяет его изменить.
   */
  public void setFixedAspectRatio(boolean fixAspectRatio) {
    mCropOverlayView.setFixedAspectRatio(fixAspectRatio);
  }

  /**==========================================================================================
   * должно ли изображение быть перевернуто по горизонтали */
  public boolean isFlippedHorizontally() {
    return mFlipHorizontally;
  }

  /**==========================================================================================
   * Устанавливает, должно ли изображение быть перевернуто по горизонтали */
  public void setFlippedHorizontally(boolean flipHorizontally) {
    if (mFlipHorizontally != flipHorizontally) {
      mFlipHorizontally = flipHorizontally;
      applyImageMatrix(getWidth(), getHeight(), true, false);
    }
  }

  /**==========================================================================================
   * нужно ли переворачивать изображение по вертикали */
  public boolean isFlippedVertically() {
    return mFlipVertically;
  }

  /**==========================================================================================
   * Устанавливает, должно ли изображение быть перевернуто вертикально */
  public void setFlippedVertically(boolean flipVertically) {
    if (mFlipVertically != flipVertically) {
      mFlipVertically = flipVertically;
      applyImageMatrix(getWidth(), getHeight(), true, false);
    }
  }

  /**==========================================================================================
   * Получите текущий набор опций направляющих. */
  public Guidelines getGuidelines() {
    return mCropOverlayView.getGuidelines();
  }

  /**==========================================================================================
   * Устанавливает направляющие для CropOverlayView, чтобы они были включены, выключены или
   * отображались при изменении размера приложения.
   */
  public void setGuidelines(Guidelines guidelines) {
    mCropOverlayView.setGuidelines(guidelines);
  }

  /**==========================================================================================
   * оба значения X и Y аспекта. */
  public Pair<Integer, Integer> getAspectRatio() {
    return new Pair<>(mCropOverlayView.getAspectRatioX(), mCropOverlayView.getAspectRatioY());
  }

  /**==========================================================================================
   * Задает значения X и Y для отношения аспектов.<br>
   * Устанавливает фиксированное соотношение сторон в TRUE.
   *
   * @param aspectRatioX int, указывающий новое значение X соотношения сторон
   * @param aspectRatioY int, указывающий новое значение Y соотношения сторон
   */
  public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
    mCropOverlayView.setAspectRatioX(aspectRatioX);
    mCropOverlayView.setAspectRatioY(aspectRatioY);
    setFixedAspectRatio(true);
  }

  /**==========================================================================================
   * Очищает установленные значения соотношения сторон и устанавливает для фиксированного
   * соотношения сторон значение FALSE. */
  public void clearAspectRatio() {
    mCropOverlayView.setAspectRatioX(1);
    mCropOverlayView.setAspectRatioY(1);
    setFixedAspectRatio(false);
  }

  /**==========================================================================================
   * Край окна обрезки будет привязан к соответствующему краю указанной ограничивающей рамки,
   * когда край окна обрезки меньше или равен этому расстоянию (в пикселях) от края
   * ограничивающей рамки. (по умолчанию: 3 дп)
   */
  public void setSnapRadius(float snapRadius) {
    if (snapRadius >= 0) {
      mCropOverlayView.setSnapRadius(snapRadius);
    }
  }

  /**==========================================================================================
   * если отображать индикатор выполнения, когда выполняется асинхронная загрузка/обрезка изображения.<br>
   * по умолчанию: true, отключите, чтобы предоставить пользовательский интерфейс индикатора выполнения.
   */
  public boolean isShowProgressBar() {
    return mShowProgressBar;
  }

  /**==========================================================================================
   * если отображать индикатор выполнения, когда выполняется асинхронная загрузка/обрезка изображения.<br>
   * по умолчанию: true, отключите, чтобы предоставить пользовательский интерфейс индикатора выполнения.
   */
  public void setShowProgressBar(boolean showProgressBar) {
    if (mShowProgressBar != showProgressBar) {
      mShowProgressBar = showProgressBar;
      setProgressBarVisibility();
    }
  }

  /**==========================================================================================
   * если показать пользовательский интерфейс наложения обрезки, который содержит
   * пользовательский интерфейс окна обрезки, окруженный фоном поверх изображения обрезки.<br>
   * по умолчанию: true, можно отключить для анимации или смены кадров.
   */
  public boolean isShowCropOverlay() {
    return mShowCropOverlay;
  }

  /**==========================================================================================
   * если показать пользовательский интерфейс наложения обрезки, который содержит
   * пользовательский интерфейс окна обрезки, окруженный фоном поверх изображения обрезки.<br>
   * по умолчанию: true, можно отключить для анимации или смены кадров.
   */
  public void setShowCropOverlay(boolean showCropOverlay) {
    if (mShowCropOverlay != showCropOverlay) {
      mShowCropOverlay = showCropOverlay;
      setCropOverlayVisibility();
    }
  }

  /**==========================================================================================
   * если сохранить растровое изображение при сохранении состояния экземпляра.<br>
   * Лучше всего этого избежать, используя URI в настройках изображения для обрезки.<br>
   * Если false, растровое изображение не сохраняется, и если для просмотра требуется
   * восстановление, оно будет пустым, сохранение растрового изображения требует сохранения
   * его в файл, что может быть дорогостоящим. по умолчанию: ложь.
   */
  public boolean isSaveBitmapToInstanceState() {
    return mSaveBitmapToInstanceState;
  }

  /**==========================================================================================
   * если сохранить растровое изображение при сохранении состояния экземпляра.<br>
   * Лучше всего этого избежать, используя URI в настройках изображения для обрезки.<br>
   * Если false, растровое изображение не сохраняется, и если для просмотра требуется
   * восстановление, оно будет пустым, сохранение растрового изображения требует сохранения его
   * в файл, что может быть дорогостоящим. по умолчанию: ложь.
   */
  public void setSaveBitmapToInstanceState(boolean saveBitmapToInstanceState) {
    mSaveBitmapToInstanceState = saveBitmapToInstanceState;
  }

  /**==========================================================================================
   * Возвращает целое число imageResource */
  public int getImageResource() {
    return mImageResource;
  }

  /**==========================================================================================
   * Получить URI изображения, который был задан URI, в противном случае — null. */
  public Uri getImageUri() {
    return mLoadedImageUri;
  }

  /**==========================================================================================
   * Получает размеры исходного растрового изображения. Это представляет собой максимально
   * возможный прямоугольник обрезки.
   *
   * @return Размеры экземпляра Rect исходного растрового изображения
   */
  public Rect getWholeImageRect() {
    int loadedSampleSize = mLoadedSampleSize;
    Bitmap bitmap = mBitmap;
    if (bitmap == null) {
      return null;
    }

    int orgWidth = bitmap.getWidth() * loadedSampleSize;
    int orgHeight = bitmap.getHeight() * loadedSampleSize;
    return new Rect(0, 0, orgWidth, orgHeight);
  }

  /**==========================================================================================
   * Получает положение окна обрезки относительно исходного растрового изображения
   * (не изображения, отображаемого в CropImageView), используя поворот исходного изображения.
   *
   * @return экземпляр Rect, содержащий границы обрезанной области исходного растрового изображения
   */
  public Rect getCropRect() {
    int loadedSampleSize = mLoadedSampleSize;
    Bitmap bitmap = mBitmap;
    if (bitmap == null) {
      return null;
    }

    // get the points of the crop rectangle adjusted to source bitmap
    float[] points = getCropPoints();

    int orgWidth = bitmap.getWidth() * loadedSampleSize;
    int orgHeight = bitmap.getHeight() * loadedSampleSize;

    // get the rectangle for the points (it may be larger than original if rotation is not stright)
    return BitmapUtils.getRectFromPoints(
            points,
            orgWidth,
            orgHeight,
            mCropOverlayView.isFixAspectRatio(),
            mCropOverlayView.getAspectRatioX(),
            mCropOverlayView.getAspectRatioY());
  }

  /**==========================================================================================
   * Получает положение окна обрезки относительно представления родителя на экране.
   *
   * @return экземпляр Rect, содержащий границы обрезанной области исходного растрового изображения
   */
  public RectF getCropWindowRect() {
    if (mCropOverlayView == null) {
      return null;
    }
    return mCropOverlayView.getCropWindowRect();
  }

  /**==========================================================================================
   * Получает 4 точки положения окна кадрирования относительно исходного растрового изображения
   * (а не изображения, отображаемого в CropImageView), используя поворот исходного изображения.<br>
   * Примечание: 4 точки могут не быть прямоугольником, если изображение было повернуто
   * НЕ под прямым углом (!=90/180/270).
   *
   * @return 4 точки (x0,y0,x1,y1,x2,y2,x3,y3) границ кадрируемой области
   */
  public float[] getCropPoints() {

    // Get crop window position relative to the displayed image.
    RectF cropWindowRect = mCropOverlayView.getCropWindowRect();

    float[] points =
            new float[] {
                    cropWindowRect.left,
                    cropWindowRect.top,
                    cropWindowRect.right,
                    cropWindowRect.top,
                    cropWindowRect.right,
                    cropWindowRect.bottom,
                    cropWindowRect.left,
                    cropWindowRect.bottom
            };

    mImageMatrix.invert(mImageInverseMatrix);
    mImageInverseMatrix.mapPoints(points);

    for (int i = 0; i < points.length; i++) {
      points[i] *= mLoadedSampleSize;
    }

    return points;
  }

  /**==========================================================================================
   * Задайте положение и размер окна обрезки для заданного прямоугольника.<br>
   * Изображение для обрезки должно быть сначала установлено перед вызовом этого,
   * для асинхронного — после полного обратного вызова.
   *
   * @param rect прямоугольник окна (положение и размер) относительно исходного растрового изображения
   */
  public void setCropRect(Rect rect) {
    mCropOverlayView.setInitialCropWindowRect(rect);
  }

  /**==========================================================================================
   * Сбросить окно обрезки до исходного прямоугольника. */
  public void resetCropRect() {
    mZoom = 1;
    mZoomOffsetX = 0;
    mZoomOffsetY = 0;
    mDegreesRotated = mInitialDegreesRotated;
    mFlipHorizontally = false;
    mFlipVertically = false;
    applyImageMatrix(getWidth(), getHeight(), false, false);
    mCropOverlayView.resetCropWindowRect();
  }

  /**==========================================================================================
   * Получает обрезанное изображение на основе текущего окна обрезки.
   *
   * @return новое растровое изображение, представляющее обрезанное изображение
   */
  public Bitmap getCroppedImage() {
    return getCroppedImage(0, 0, RequestSizeOptions.NONE);
  }

  /**==========================================================================================
   * Получает обрезанное изображение на основе текущего окна кадрирования.<br>
   * Использует параметр {@link RequestSizeOptions#RESIZE_INSIDE}.
   *
   * @param reqWidth the width to resize the cropped image to
   * @param reqHeight the height to resize the cropped image to
   * @return a new Bitmap representing the cropped image
   */
  public Bitmap getCroppedImage(int reqWidth, int reqHeight) {
    return getCroppedImage(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
  }

  /**==========================================================================================
   * Получает обрезанное изображение на основе текущего окна кадрирования.<br>
   *
   * @param reqWidth the width to resize the cropped image to (see options)
   * @param reqHeight the height to resize the cropped image to (see options)
   * @param options the resize method to use, see its documentation
   * @return a new Bitmap representing the cropped image
   */
  public Bitmap getCroppedImage(int reqWidth, int reqHeight, RequestSizeOptions options) {
    Bitmap croppedBitmap = null;
    if (mBitmap != null) {
      mImageView.clearAnimation();

      reqWidth = options != RequestSizeOptions.NONE ? reqWidth : 0;
      reqHeight = options != RequestSizeOptions.NONE ? reqHeight : 0;

      if (mLoadedImageUri != null
              && (mLoadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING)) {
        int orgWidth = mBitmap.getWidth() * mLoadedSampleSize;
        int orgHeight = mBitmap.getHeight() * mLoadedSampleSize;
        BitmapUtils.BitmapSampled bitmapSampled =
                BitmapUtils.cropBitmap(
                        getContext(),
                        mLoadedImageUri,
                        getCropPoints(),
                        mDegreesRotated,
                        orgWidth,
                        orgHeight,
                        mCropOverlayView.isFixAspectRatio(),
                        mCropOverlayView.getAspectRatioX(),
                        mCropOverlayView.getAspectRatioY(),
                        reqWidth,
                        reqHeight,
                        mFlipHorizontally,
                        mFlipVertically);
        croppedBitmap = bitmapSampled.bitmap;
      } else {
        croppedBitmap =
                BitmapUtils.cropBitmapObjectHandleOOM(
                        mBitmap,
                        getCropPoints(),
                        mDegreesRotated,
                        mCropOverlayView.isFixAspectRatio(),
                        mCropOverlayView.getAspectRatioX(),
                        mCropOverlayView.getAspectRatioY(),
                        mFlipHorizontally,
                        mFlipVertically)
                        .bitmap;
      }

      croppedBitmap = BitmapUtils.resizeBitmap(croppedBitmap, reqWidth, reqHeight, options);
    }

    return croppedBitmap;
  }

  /**==========================================================================================
   * Получает обрезанное изображение на основе текущего окна кадрирования.<br>
   * Результат будет вызван для прослушивателя,
   * установленного {@link#setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   */
  public void getCroppedImageAsync() {
    getCroppedImageAsync(0, 0, RequestSizeOptions.NONE);
  }

  /**==========================================================================================
   * Получает обрезанное изображение на основе текущего окна кадрирования.<br>
   * Использует параметр {@link RequestSizeOptions#RESIZE_INSIDE}.<br>
   * Результат будет вызван для прослушивателя,
   * установленного {@link#setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param reqWidth the width to resize the cropped image to
   * @param reqHeight the height to resize the cropped image to
   */
  public void getCroppedImageAsync(int reqWidth, int reqHeight) {
    getCroppedImageAsync(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE);
  }

  /**==========================================================================================
   * Gets the cropped image based on the current crop window.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param reqWidth the width to resize the cropped image to (see options)
   * @param reqHeight the height to resize the cropped image to (see options)
   * @param options the resize method to use, see its documentation
   */
  public void getCroppedImageAsync(int reqWidth, int reqHeight, RequestSizeOptions options) {
    if (mOnCropImageCompleteListener == null) {
      throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
    }
    startCropWorkerTask(reqWidth, reqHeight, options, null, null, 0);
  }

  /**==========================================================================================
   * Save the cropped image based on the current crop window to the given uri.<br>
   * Uses JPEG image compression with 90 compression quality.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param saveUri the Android Uri to save the cropped image to
   */
  public void saveCroppedImageAsync(Uri saveUri) {
    saveCroppedImageAsync(saveUri, Bitmap.CompressFormat.JPEG, 90, 0, 0, RequestSizeOptions.NONE);
  }

  /**==========================================================================================
   * Save the cropped image based on the current crop window to the given uri.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param saveUri the Android Uri to save the cropped image to
   * @param saveCompressFormat the compression format to use when writing the image
   * @param saveCompressQuality the quality (if applicable) to use when writing the image (0 - 100)
   */
  public void saveCroppedImageAsync(
          Uri saveUri, Bitmap.CompressFormat saveCompressFormat, int saveCompressQuality) {
    saveCroppedImageAsync(
            saveUri, saveCompressFormat, saveCompressQuality, 0, 0, RequestSizeOptions.NONE);
  }

  /**==========================================================================================
   * Save the cropped image based on the current crop window to the given uri.<br>
   * Uses {@link RequestSizeOptions#RESIZE_INSIDE} option.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param saveUri the Android Uri to save the cropped image to
   * @param saveCompressFormat the compression format to use when writing the image
   * @param saveCompressQuality the quality (if applicable) to use when writing the image (0 - 100)
   * @param reqWidth the width to resize the cropped image to
   * @param reqHeight the height to resize the cropped image to
   */
  public void saveCroppedImageAsync(
          Uri saveUri,
          Bitmap.CompressFormat saveCompressFormat,
          int saveCompressQuality,
          int reqWidth,
          int reqHeight) {
    saveCroppedImageAsync(
            saveUri,
            saveCompressFormat,
            saveCompressQuality,
            reqWidth,
            reqHeight,
            RequestSizeOptions.RESIZE_INSIDE);
  }

  /**==========================================================================================
   * Save the cropped image based on the current crop window to the given uri.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param saveUri the Android Uri to save the cropped image to
   * @param saveCompressFormat the compression format to use when writing the image
   * @param saveCompressQuality the quality (if applicable) to use when writing the image (0 - 100)
   * @param reqWidth the width to resize the cropped image to (see options)
   * @param reqHeight the height to resize the cropped image to (see options)
   * @param options the resize method to use, see its documentation
   */
  public void saveCroppedImageAsync(
          Uri saveUri,
          Bitmap.CompressFormat saveCompressFormat,
          int saveCompressQuality,
          int reqWidth,
          int reqHeight,
          RequestSizeOptions options) {
    if (mOnCropImageCompleteListener == null) {
      throw new IllegalArgumentException("mOnCropImageCompleteListener is not set");
    }
    startCropWorkerTask(
            reqWidth, reqHeight, options, saveUri, saveCompressFormat, saveCompressQuality);
  }

  /**==========================================================================================
   * Set the callback t */
  public void setOnSetCropOverlayReleasedListener(OnSetCropOverlayReleasedListener listener) {
    mOnCropOverlayReleasedListener = listener;
  }

  /**==========================================================================================
   * Set the callback when the cropping is moved */
  public void setOnSetCropOverlayMovedListener(OnSetCropOverlayMovedListener listener) {
    mOnSetCropOverlayMovedListener = listener;
  }

  /**==========================================================================================
   * Set the callback when the crop window is changed */
  public void setOnCropWindowChangedListener(OnSetCropWindowChangeListener listener) {
    mOnSetCropWindowChangeListener = listener;
  }

  /**==========================================================================================
   * Set the callback to be invoked when image async loading ({@link #setImageUriAsync(Uri)}) is
   * complete (successful or failed).
   */
  public void setOnSetImageUriCompleteListener(OnSetImageUriCompleteListener listener) {
    mOnSetImageUriCompleteListener = listener;
  }

  /**==========================================================================================
   * Set the callback to be invoked when image async cropping image ({@link #getCroppedImageAsync()}
   * or {@link #saveCroppedImageAsync(Uri)}) is complete (successful or failed).
   */
  public void setOnCropImageCompleteListener(OnCropImageCompleteListener listener) {
    mOnCropImageCompleteListener = listener;
  }

  /**==========================================================================================
   * Sets a Bitmap as the content of the CropImageView.
   *
   * @param bitmap the Bitmap to set
   */
  public void setImageBitmap(Bitmap bitmap) {
    mCropOverlayView.setInitialCropWindowRect(null);
    setBitmap(bitmap, 0, null, 1, 0);
  }

  /**==========================================================================================
   * Sets a Bitmap and initializes the image rotation according to the EXIT data.<br>
   * <br>
   * The EXIF can be retrieved by doing the following: <code>
   * ExifInterface exif = new ExifInterface(path);</code>
   *
   * @param bitmap the original bitmap to set; if null, this
   * @param exif the EXIF information about this bitmap; may be null
   */
  public void setImageBitmap(Bitmap bitmap, ExifInterface exif) {
    Bitmap setBitmap;
    int degreesRotated = 0;
    if (bitmap != null && exif != null) {
      BitmapUtils.RotateBitmapResult result = BitmapUtils.rotateBitmapByExif(bitmap, exif);
      setBitmap = result.bitmap;
      degreesRotated = result.degrees;
      mInitialDegreesRotated = result.degrees;
    } else {
      setBitmap = bitmap;
    }
    mCropOverlayView.setInitialCropWindowRect(null);
    setBitmap(setBitmap, 0, null, 1, degreesRotated);
  }

  /**==========================================================================================
   * Sets a Drawable as the content of the CropImageView.
   *
   * @param resId the drawable resource ID to set
   */
  public void setImageResource(int resId) {
    if (resId != 0) {
      mCropOverlayView.setInitialCropWindowRect(null);
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
      setBitmap(bitmap, resId, null, 1, 0);
    }
  }

  /**==========================================================================================
   * Задает растровое изображение, загруженное из указанного URI Android, в качестве содержимого CropImageView.<br>
   * Можно использовать с URI из галереи или источника с камеры.<br>
   * Повернет изображение по данным exif.<br>
   * <br>
   * Sets a bitmap loaded from the given Android URI as the content of the CropImageView.<br>
   * Can be used with URI from gallery or camera source.<br>
   * Will rotate the image by exif data.<br>
   *
   * @param uri URI для загрузки изображения из
   */
  public void setImageUriAsync(Uri uri) {
    if (uri != null) {
      BitmapLoadingWorkerTask currentTask =
              mBitmapLoadingWorkerTask != null ? mBitmapLoadingWorkerTask.get() : null;
      if (currentTask != null) {
        // отменить предыдущую загрузку (не проверять один и тот же URI, потому что URI камеры
        // может быть одинаковым для разных изображений)
        currentTask.cancel(true);
      }

      // либо существующая задача не работает, либо мы отменили ее, необходимо загрузить новый URI
      clearImageInt();
      mRestoreCropWindowRect = null;
      mRestoreDegreesRotated = 0;
      mCropOverlayView.setInitialCropWindowRect(null);
      mBitmapLoadingWorkerTask = new WeakReference<>(new BitmapLoadingWorkerTask(this, uri));
      mBitmapLoadingWorkerTask.get().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      setProgressBarVisibility();
    }
  }

  /**==========================================================================================
   * Clear the current image set for cropping. */
  public void clearImage() {
    clearImageInt();
    mCropOverlayView.setInitialCropWindowRect(null);
  }

  /**==========================================================================================
   * Rotates image by the specified number of degrees clockwise.<br>
   * Negative values represent counter-clockwise rotations.
   *
   * @param degrees Integer specifying the number of degrees to rotate.
   */
  public void rotateImage(int degrees) {
    if (mBitmap != null) {
      // Force degrees to be a non-zero value between 0 and 360 (inclusive)
      if (degrees < 0) {
        degrees = (degrees % 360) + 360;
      } else {
        degrees = degrees % 360;
      }

      boolean flipAxes =
              !mCropOverlayView.isFixAspectRatio()
                      && ((degrees > 45 && degrees < 135) || (degrees > 215 && degrees < 305));
      BitmapUtils.RECT.set(mCropOverlayView.getCropWindowRect());
      float halfWidth = (flipAxes ? BitmapUtils.RECT.height() : BitmapUtils.RECT.width()) / 2f;
      float halfHeight = (flipAxes ? BitmapUtils.RECT.width() : BitmapUtils.RECT.height()) / 2f;
      if (flipAxes) {
        boolean isFlippedHorizontally = mFlipHorizontally;
        mFlipHorizontally = mFlipVertically;
        mFlipVertically = isFlippedHorizontally;
      }

      mImageMatrix.invert(mImageInverseMatrix);

      BitmapUtils.POINTS[0] = BitmapUtils.RECT.centerX();
      BitmapUtils.POINTS[1] = BitmapUtils.RECT.centerY();
      BitmapUtils.POINTS[2] = 0;
      BitmapUtils.POINTS[3] = 0;
      BitmapUtils.POINTS[4] = 1;
      BitmapUtils.POINTS[5] = 0;
      mImageInverseMatrix.mapPoints(BitmapUtils.POINTS);

      // This is valid because degrees is not negative.
      mDegreesRotated = (mDegreesRotated + degrees) % 360;

      applyImageMatrix(getWidth(), getHeight(), true, false);

      // adjust the zoom so the crop window size remains the same even after image scale change
      mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);
      mZoom /=
              Math.sqrt(
                      Math.pow(BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2], 2)
                              + Math.pow(BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3], 2));
      mZoom = Math.max(mZoom, 1);

      applyImageMatrix(getWidth(), getHeight(), true, false);

      mImageMatrix.mapPoints(BitmapUtils.POINTS2, BitmapUtils.POINTS);

      // adjust the width/height by the changes in scaling to the image
      double change =
              Math.sqrt(
                      Math.pow(BitmapUtils.POINTS2[4] - BitmapUtils.POINTS2[2], 2)
                              + Math.pow(BitmapUtils.POINTS2[5] - BitmapUtils.POINTS2[3], 2));
      halfWidth *= change;
      halfHeight *= change;

      // calculate the new crop window rectangle to center in the same location and have proper
      // width/height
      BitmapUtils.RECT.set(
              BitmapUtils.POINTS2[0] - halfWidth,
              BitmapUtils.POINTS2[1] - halfHeight,
              BitmapUtils.POINTS2[0] + halfWidth,
              BitmapUtils.POINTS2[1] + halfHeight);

      mCropOverlayView.resetCropOverlayView();
      mCropOverlayView.setCropWindowRect(BitmapUtils.RECT);
      applyImageMatrix(getWidth(), getHeight(), true, false);
      handleCropWindowChanged(false, false);

      // make sure the crop window rectangle is within the cropping image bounds after all the
      // changes
      mCropOverlayView.fixCurrentCropWindowRect();
    }
  }

  /**==========================================================================================
   * Flips the image horizontally. */
  public void flipImageHorizontally() {
    mFlipHorizontally = !mFlipHorizontally;
    applyImageMatrix(getWidth(), getHeight(), true, false);
  }

  /**==========================================================================================
   * Flips the image vertically. */
  public void flipImageVertically() {
    mFlipVertically = !mFlipVertically;
    applyImageMatrix(getWidth(), getHeight(), true, false);
  }

  //=============================================================================
  // Частные методы
  //=============================================================================

  /**==========================================================================================
   * On complete of the async bitmap loading by {@link #setImageUriAsync(Uri)} set the result to the
   * widget if still relevant and call listener if set.
   *
   * @param result the result of bitmap loading
   */
  void onSetImageUriAsyncComplete(BitmapLoadingWorkerTask.Result result) {

    mBitmapLoadingWorkerTask = null;
    setProgressBarVisibility();

    if (result.error == null) {
      mInitialDegreesRotated = result.degreesRotated;
      setBitmap(result.bitmap, 0, result.uri, result.loadSampleSize, result.degreesRotated);
    }

    OnSetImageUriCompleteListener listener = mOnSetImageUriCompleteListener;
    if (listener != null) {
      listener.onSetImageUriComplete(this, result.uri, result.error);
    }
  }

  /**==========================================================================================
   * On complete of the async bitmap cropping by {@link #getCroppedImageAsync()} call listener if
   * set.
   *
   * @param result the result of bitmap cropping
   */
  void onImageCroppingAsyncComplete(BitmapCroppingWorkerTask.Result result) {

    mBitmapCroppingWorkerTask = null;
    setProgressBarVisibility();

    OnCropImageCompleteListener listener = mOnCropImageCompleteListener;
    if (listener != null) {
      CropResult cropResult =
              new CropResult(
                      mBitmap,
                      mLoadedImageUri,
                      result.bitmap,
                      result.uri,
                      result.error,
                      getCropPoints(),
                      getCropRect(),
                      getWholeImageRect(),
                      getRotatedDegrees(),
                      result.sampleSize);
      listener.onCropImageComplete(this, cropResult);
    }
  }

  /**==========================================================================================
   * Set the given bitmap to be used in for cropping<br>
   * Optionally clear full if the bitmap is new, or partial clear if the bitmap has been
   * manipulated.
   */
  private void setBitmap(
          Bitmap bitmap, int imageResource, Uri imageUri, int loadSampleSize, int degreesRotated) {
    if (mBitmap == null || !mBitmap.equals(bitmap)) {

      mImageView.clearAnimation();

      clearImageInt();

      mBitmap = bitmap;
      mImageView.setImageBitmap(mBitmap);

      mLoadedImageUri = imageUri;
      mImageResource = imageResource;
      mLoadedSampleSize = loadSampleSize;
      mDegreesRotated = degreesRotated;

      applyImageMatrix(getWidth(), getHeight(), true, false);

      if (mCropOverlayView != null) {
        mCropOverlayView.resetCropOverlayView();
        setCropOverlayVisibility();
      }
    }
  }

  /**==========================================================================================
   * Clear the current image set for cropping.<br>
   * Full clear will also clear the data of the set image like Uri or Resource id while partial
   * clear will only clear the bitmap and recycle if required.
   */
  private void clearImageInt() {

    // if we allocated the bitmap, release it as fast as possible
    if (mBitmap != null && (mImageResource > 0 || mLoadedImageUri != null)) {
      mBitmap.recycle();
    }
    mBitmap = null;

    // clean the loaded image flags for new image
    mImageResource = 0;
    mLoadedImageUri = null;
    mLoadedSampleSize = 1;
    mDegreesRotated = 0;
    mZoom = 1;
    mZoomOffsetX = 0;
    mZoomOffsetY = 0;
    mImageMatrix.reset();
    mSaveInstanceStateBitmapUri = null;

    mImageView.setImageBitmap(null);

    setCropOverlayVisibility();
  }

  /**==========================================================================================
   * Gets the cropped image based on the current crop window.<br>
   * If (reqWidth,reqHeight) is given AND image is loaded from URI cropping will try to use sample
   * size to fit in the requested width and height down-sampling if possible - optimization to get
   * best size to quality.<br>
   * The result will be invoked to listener set by {@link
   * #setOnCropImageCompleteListener(OnCropImageCompleteListener)}.
   *
   * @param reqWidth the width to resize the cropped image to (see options)
   * @param reqHeight the height to resize the cropped image to (see options)
   * @param options the resize method to use on the cropped bitmap
   * @param saveUri optional: to save the cropped image to
   * @param saveCompressFormat if saveUri is given, the given compression will be used for saving
   *     the image
   * @param saveCompressQuality if saveUri is given, the given quality will be used for the
   *     compression.
   */
  public void startCropWorkerTask(
          int reqWidth,
          int reqHeight,
          RequestSizeOptions options,
          Uri saveUri,
          Bitmap.CompressFormat saveCompressFormat,
          int saveCompressQuality) {
    Bitmap bitmap = mBitmap;
    if (bitmap != null) {
      mImageView.clearAnimation();

      BitmapCroppingWorkerTask currentTask =
              mBitmapCroppingWorkerTask != null ? mBitmapCroppingWorkerTask.get() : null;
      if (currentTask != null) {
        // cancel previous cropping
        currentTask.cancel(true);
      }

      reqWidth = options != RequestSizeOptions.NONE ? reqWidth : 0;
      reqHeight = options != RequestSizeOptions.NONE ? reqHeight : 0;

      int orgWidth = bitmap.getWidth() * mLoadedSampleSize;
      int orgHeight = bitmap.getHeight() * mLoadedSampleSize;
      if (mLoadedImageUri != null
              && (mLoadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING)) {
        mBitmapCroppingWorkerTask =
                new WeakReference<>(
                        new BitmapCroppingWorkerTask(
                                this,
                                mLoadedImageUri,
                                getCropPoints(),
                                mDegreesRotated,
                                orgWidth,
                                orgHeight,
                                mCropOverlayView.isFixAspectRatio(),
                                mCropOverlayView.getAspectRatioX(),
                                mCropOverlayView.getAspectRatioY(),
                                reqWidth,
                                reqHeight,
                                mFlipHorizontally,
                                mFlipVertically,
                                options,
                                saveUri,
                                saveCompressFormat,
                                saveCompressQuality));
      } else {
        mBitmapCroppingWorkerTask =
                new WeakReference<>(
                        new BitmapCroppingWorkerTask(
                                this,
                                bitmap,
                                getCropPoints(),
                                mDegreesRotated,
                                mCropOverlayView.isFixAspectRatio(),
                                mCropOverlayView.getAspectRatioX(),
                                mCropOverlayView.getAspectRatioY(),
                                reqWidth,
                                reqHeight,
                                mFlipHorizontally,
                                mFlipVertically,
                                options,
                                saveUri,
                                saveCompressFormat,
                                saveCompressQuality));
      }
      mBitmapCroppingWorkerTask.get().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      setProgressBarVisibility();
    }
  }

  @Override
  public Parcelable onSaveInstanceState() {
    if (mLoadedImageUri == null && mBitmap == null && mImageResource < 1) {
      return super.onSaveInstanceState();
    }

    Bundle bundle = new Bundle();
    Uri imageUri = mLoadedImageUri;
    if (mSaveBitmapToInstanceState && imageUri == null && mImageResource < 1) {
      mSaveInstanceStateBitmapUri =
              imageUri =
                      BitmapUtils.writeTempStateStoreBitmap(
                              getContext(), mBitmap, mSaveInstanceStateBitmapUri);
    }
    if (imageUri != null && mBitmap != null) {
      String key = UUID.randomUUID().toString();
      BitmapUtils.mStateBitmap = new Pair<>(key, new WeakReference<>(mBitmap));
      bundle.putString("LOADED_IMAGE_STATE_BITMAP_KEY", key);
    }
    if (mBitmapLoadingWorkerTask != null) {
      BitmapLoadingWorkerTask task = mBitmapLoadingWorkerTask.get();
      if (task != null) {
        bundle.putParcelable("LOADING_IMAGE_URI", task.getUri());
      }
    }
    bundle.putParcelable("instanceState", super.onSaveInstanceState());
    bundle.putParcelable("LOADED_IMAGE_URI", imageUri);
    bundle.putInt("LOADED_IMAGE_RESOURCE", mImageResource);
    bundle.putInt("LOADED_SAMPLE_SIZE", mLoadedSampleSize);
    bundle.putInt("DEGREES_ROTATED", mDegreesRotated);
    bundle.putParcelable("INITIAL_CROP_RECT", mCropOverlayView.getInitialCropWindowRect());

    BitmapUtils.RECT.set(mCropOverlayView.getCropWindowRect());

    mImageMatrix.invert(mImageInverseMatrix);
    mImageInverseMatrix.mapRect(BitmapUtils.RECT);

    bundle.putParcelable("CROP_WINDOW_RECT", BitmapUtils.RECT);
    bundle.putString("CROP_SHAPE", mCropOverlayView.getCropShape().name());
    bundle.putBoolean("CROP_AUTO_ZOOM_ENABLED", mAutoZoomEnabled);
    bundle.putInt("CROP_MAX_ZOOM", mMaxZoom);
    bundle.putBoolean("CROP_FLIP_HORIZONTALLY", mFlipHorizontally);
    bundle.putBoolean("CROP_FLIP_VERTICALLY", mFlipVertically);

    return bundle;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {

    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;

      // prevent restoring state if already set by outside code
      if (mBitmapLoadingWorkerTask == null
              && mLoadedImageUri == null
              && mBitmap == null
              && mImageResource == 0) {

        Uri uri = bundle.getParcelable("LOADED_IMAGE_URI");
        if (uri != null) {
          String key = bundle.getString("LOADED_IMAGE_STATE_BITMAP_KEY");
          if (key != null) {
            Bitmap stateBitmap =
                    BitmapUtils.mStateBitmap != null && BitmapUtils.mStateBitmap.first.equals(key)
                            ? BitmapUtils.mStateBitmap.second.get()
                            : null;
            BitmapUtils.mStateBitmap = null;
            if (stateBitmap != null && !stateBitmap.isRecycled()) {
              setBitmap(stateBitmap, 0, uri, bundle.getInt("LOADED_SAMPLE_SIZE"), 0);
            }
          }
          if (mLoadedImageUri == null) {
            setImageUriAsync(uri);
          }
        } else {
          int resId = bundle.getInt("LOADED_IMAGE_RESOURCE");
          if (resId > 0) {
            setImageResource(resId);
          } else {
            uri = bundle.getParcelable("LOADING_IMAGE_URI");
            if (uri != null) {
              setImageUriAsync(uri);
            }
          }
        }

        mDegreesRotated = mRestoreDegreesRotated = bundle.getInt("DEGREES_ROTATED");

        Rect initialCropRect = bundle.getParcelable("INITIAL_CROP_RECT");
        if (initialCropRect != null
                && (initialCropRect.width() > 0 || initialCropRect.height() > 0)) {
          mCropOverlayView.setInitialCropWindowRect(initialCropRect);
        }

        RectF cropWindowRect = bundle.getParcelable("CROP_WINDOW_RECT");
        if (cropWindowRect != null && (cropWindowRect.width() > 0 || cropWindowRect.height() > 0)) {
          mRestoreCropWindowRect = cropWindowRect;
        }

        mCropOverlayView.setCropShape(CropShape.valueOf(bundle.getString("CROP_SHAPE")));

        mAutoZoomEnabled = bundle.getBoolean("CROP_AUTO_ZOOM_ENABLED");
        mMaxZoom = bundle.getInt("CROP_MAX_ZOOM");

        mFlipHorizontally = bundle.getBoolean("CROP_FLIP_HORIZONTALLY");
        mFlipVertically = bundle.getBoolean("CROP_FLIP_VERTICALLY");
      }

      super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    if (mBitmap != null) {

      // Bypasses a baffling bug when used within a ScrollView, where heightSize is set to 0.
      if (heightSize == 0) {
        heightSize = mBitmap.getHeight();
      }

      int desiredWidth;
      int desiredHeight;

      double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
      double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

      // Checks if either width or height needs to be fixed
      if (widthSize < mBitmap.getWidth()) {
        viewToBitmapWidthRatio = (double) widthSize / (double) mBitmap.getWidth();
      }
      if (heightSize < mBitmap.getHeight()) {
        viewToBitmapHeightRatio = (double) heightSize / (double) mBitmap.getHeight();
      }

      // If either needs to be fixed, choose smallest ratio and calculate from there
      if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY
              || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
        if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
          desiredWidth = widthSize;
          desiredHeight = (int) (mBitmap.getHeight() * viewToBitmapWidthRatio);
        } else {
          desiredHeight = heightSize;
          desiredWidth = (int) (mBitmap.getWidth() * viewToBitmapHeightRatio);
        }
      } else {
        // Otherwise, the picture is within frame layout bounds. Desired width is simply picture
        // size
        desiredWidth = mBitmap.getWidth();
        desiredHeight = mBitmap.getHeight();
      }

      int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
      int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);

      mLayoutWidth = width;
      mLayoutHeight = height;

      setMeasuredDimension(mLayoutWidth, mLayoutHeight);

    } else {
      setMeasuredDimension(widthSize, heightSize);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {

    super.onLayout(changed, l, t, r, b);

    if (mLayoutWidth > 0 && mLayoutHeight > 0) {
      // Gets original parameters, and creates the new parameters
      ViewGroup.LayoutParams origParams = this.getLayoutParams();
      origParams.width = mLayoutWidth;
      origParams.height = mLayoutHeight;
      setLayoutParams(origParams);

      if (mBitmap != null) {
        applyImageMatrix(r - l, b - t, true, false);

        // after state restore we want to restore the window crop, possible only after widget size
        // is known
        if (mRestoreCropWindowRect != null) {
          if (mRestoreDegreesRotated != mInitialDegreesRotated) {
            mDegreesRotated = mRestoreDegreesRotated;
            applyImageMatrix(r - l, b - t, true, false);
          }
          mImageMatrix.mapRect(mRestoreCropWindowRect);
          mCropOverlayView.setCropWindowRect(mRestoreCropWindowRect);
          handleCropWindowChanged(false, false);
          mCropOverlayView.fixCurrentCropWindowRect();
          mRestoreCropWindowRect = null;
        } else if (mSizeChanged) {
          mSizeChanged = false;
          handleCropWindowChanged(false, false);
        }
      } else {
        updateImageBounds(true);
      }
    } else {
      updateImageBounds(true);
    }
  }

  /**
   * Detect size change to handle auto-zoom using {@link #handleCropWindowChanged(boolean, boolean)}
   * in {@link #layout(int, int, int, int)}.
   */
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    mSizeChanged = oldw > 0 && oldh > 0;
  }

  /**
   * Handle crop window change to:<br>
   * 1. Execute auto-zoom-in/out depending on the area covered of cropping window relative to the
   * available view area.<br>
   * 2. Slide the zoomed sub-area if the cropping window is outside of the visible view sub-area.
   * <br>
   *
   * @param inProgress is the crop window change is still in progress by the user
   * @param animate if to animate the change to the image matrix, or set it directly
   */
  private void handleCropWindowChanged(boolean inProgress, boolean animate) {
    int width = getWidth();
    int height = getHeight();
    if (mBitmap != null && width > 0 && height > 0) {

      RectF cropRect = mCropOverlayView.getCropWindowRect();
      if (inProgress) {
        if (cropRect.left < 0
                || cropRect.top < 0
                || cropRect.right > width
                || cropRect.bottom > height) {
          applyImageMatrix(width, height, false, false);
        }
      } else if (mAutoZoomEnabled || mZoom > 1) {
        float newZoom = 0;
        // keep the cropping window covered area to 50%-65% of zoomed sub-area
        if (mZoom < mMaxZoom
                && cropRect.width() < width * 0.5f
                && cropRect.height() < height * 0.5f) {
          newZoom =
                  Math.min(
                          mMaxZoom,
                          Math.min(
                                  width / (cropRect.width() / mZoom / 0.64f),
                                  height / (cropRect.height() / mZoom / 0.64f)));
        }
        if (mZoom > 1 && (cropRect.width() > width * 0.65f || cropRect.height() > height * 0.65f)) {
          newZoom =
                  Math.max(
                          1,
                          Math.min(
                                  width / (cropRect.width() / mZoom / 0.51f),
                                  height / (cropRect.height() / mZoom / 0.51f)));
        }
        if (!mAutoZoomEnabled) {
          newZoom = 1;
        }

        if (newZoom > 0 && newZoom != mZoom) {
          if (animate) {
            if (mAnimation == null) {
              // lazy create animation single instance
              mAnimation = new CropImageAnimation(mImageView, mCropOverlayView);
            }
            // set the state for animation to start from
            mAnimation.setStartState(mImagePoints, mImageMatrix);
          }

          mZoom = newZoom;

          applyImageMatrix(width, height, true, animate);
        }
      }
      if (mOnSetCropWindowChangeListener != null && !inProgress) {
        mOnSetCropWindowChangeListener.onCropWindowChanged();
      }
    }
  }

  /**
   * Apply matrix to handle the image inside the image view.
   *
   * @param width the width of the image view
   * @param height the height of the image view
   */
  private void applyImageMatrix(float width, float height, boolean center, boolean animate) {
    if (mBitmap != null && width > 0 && height > 0) {

      mImageMatrix.invert(mImageInverseMatrix);
      RectF cropRect = mCropOverlayView.getCropWindowRect();
      mImageInverseMatrix.mapRect(cropRect);

      mImageMatrix.reset();

      // move the image to the center of the image view first so we can manipulate it from there
      mImageMatrix.postTranslate(
              (width - mBitmap.getWidth()) / 2, (height - mBitmap.getHeight()) / 2);
      mapImagePointsByImageMatrix();

      // rotate the image the required degrees from center of image
      if (mDegreesRotated > 0) {
        mImageMatrix.postRotate(
                mDegreesRotated,
                BitmapUtils.getRectCenterX(mImagePoints),
                BitmapUtils.getRectCenterY(mImagePoints));
        mapImagePointsByImageMatrix();
      }

      // scale the image to the image view, image rect transformed to know new width/height
      float scale =
              Math.min(
                      width / BitmapUtils.getRectWidth(mImagePoints),
                      height / BitmapUtils.getRectHeight(mImagePoints));
      if (mScaleType == ScaleType.FIT_CENTER
              || (mScaleType == ScaleType.CENTER_INSIDE && scale < 1)
              || (scale > 1 && mAutoZoomEnabled)) {
        mImageMatrix.postScale(
                scale,
                scale,
                BitmapUtils.getRectCenterX(mImagePoints),
                BitmapUtils.getRectCenterY(mImagePoints));
        mapImagePointsByImageMatrix();
      }

      // scale by the current zoom level
      float scaleX = mFlipHorizontally ? -mZoom : mZoom;
      float scaleY = mFlipVertically ? -mZoom : mZoom;
      mImageMatrix.postScale(
              scaleX,
              scaleY,
              BitmapUtils.getRectCenterX(mImagePoints),
              BitmapUtils.getRectCenterY(mImagePoints));
      mapImagePointsByImageMatrix();

      mImageMatrix.mapRect(cropRect);

      if (center) {
        // set the zoomed area to be as to the center of cropping window as possible
        mZoomOffsetX =
                width > BitmapUtils.getRectWidth(mImagePoints)
                        ? 0
                        : Math.max(
                        Math.min(
                                width / 2 - cropRect.centerX(), -BitmapUtils.getRectLeft(mImagePoints)),
                        getWidth() - BitmapUtils.getRectRight(mImagePoints))
                        / scaleX;
        mZoomOffsetY =
                height > BitmapUtils.getRectHeight(mImagePoints)
                        ? 0
                        : Math.max(
                        Math.min(
                                height / 2 - cropRect.centerY(), -BitmapUtils.getRectTop(mImagePoints)),
                        getHeight() - BitmapUtils.getRectBottom(mImagePoints))
                        / scaleY;
      } else {
        // adjust the zoomed area so the crop window rectangle will be inside the area in case it
        // was moved outside
        mZoomOffsetX =
                Math.min(Math.max(mZoomOffsetX * scaleX, -cropRect.left), -cropRect.right + width)
                        / scaleX;
        mZoomOffsetY =
                Math.min(Math.max(mZoomOffsetY * scaleY, -cropRect.top), -cropRect.bottom + height)
                        / scaleY;
      }

      // apply to zoom offset translate and update the crop rectangle to offset correctly
      mImageMatrix.postTranslate(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY);
      cropRect.offset(mZoomOffsetX * scaleX, mZoomOffsetY * scaleY);
      mCropOverlayView.setCropWindowRect(cropRect);
      mapImagePointsByImageMatrix();
      mCropOverlayView.invalidate();

      // set matrix to apply
      if (animate) {
        // set the state for animation to end in, start animation now
        mAnimation.setEndState(mImagePoints, mImageMatrix);
        mImageView.startAnimation(mAnimation);
      } else {
        mImageView.setImageMatrix(mImageMatrix);
      }

      // update the image rectangle in the crop overlay
      updateImageBounds(false);
    }
  }

  /**
   * Adjust the given image rectangle by image transformation matrix to know the final rectangle of
   * the image.<br>
   * To get the proper rectangle it must be first reset to original image rectangle.
   */
  private void mapImagePointsByImageMatrix() {
    mImagePoints[0] = 0;
    mImagePoints[1] = 0;
    mImagePoints[2] = mBitmap.getWidth();
    mImagePoints[3] = 0;
    mImagePoints[4] = mBitmap.getWidth();
    mImagePoints[5] = mBitmap.getHeight();
    mImagePoints[6] = 0;
    mImagePoints[7] = mBitmap.getHeight();
    mImageMatrix.mapPoints(mImagePoints);
    mScaleImagePoints[0] = 0;
    mScaleImagePoints[1] = 0;
    mScaleImagePoints[2] = 100;
    mScaleImagePoints[3] = 0;
    mScaleImagePoints[4] = 100;
    mScaleImagePoints[5] = 100;
    mScaleImagePoints[6] = 0;
    mScaleImagePoints[7] = 100;
    mImageMatrix.mapPoints(mScaleImagePoints);
  }

  /**
   * Determines the specs for the onMeasure function. Calculates the width or height depending on
   * the mode.
   *
   * @param measureSpecMode The mode of the measured width or height.
   * @param measureSpecSize The size of the measured width or height.
   * @param desiredSize The desired size of the measured width or height.
   * @return The final size of the width or height.
   */
  private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {

    // Measure Width
    int spec;
    if (measureSpecMode == MeasureSpec.EXACTLY) {
      // Must be this size
      spec = measureSpecSize;
    } else if (measureSpecMode == MeasureSpec.AT_MOST) {
      // Can't be bigger than...; match_parent value
      spec = Math.min(desiredSize, measureSpecSize);
    } else {
      // Be whatever you want; wrap_content
      spec = desiredSize;
    }

    return spec;
  }

  /**
   * Set visibility of crop overlay to hide it when there is no image or specificly set by client.
   */
  private void setCropOverlayVisibility() {
    if (mCropOverlayView != null) {
      mCropOverlayView.setVisibility(mShowCropOverlay && mBitmap != null ? VISIBLE : INVISIBLE);
    }
  }

  /**
   * Set visibility of progress bar when async loading/cropping is in process and show is enabled.
   */
  private void setProgressBarVisibility() {
    boolean visible =
            mShowProgressBar
                    && (mBitmap == null && mBitmapLoadingWorkerTask != null
                    || mBitmapCroppingWorkerTask != null);
    mProgressBar.setVisibility(visible ? VISIBLE : INVISIBLE);
  }

  /** Update the scale factor between the actual image bitmap and the shown image.<br> */
  private void updateImageBounds(boolean clear) {
    if (mBitmap != null && !clear) {

      // Get the scale factor between the actual Bitmap dimensions and the displayed dimensions for
      // width/height.
      float scaleFactorWidth =
              100f * mLoadedSampleSize / BitmapUtils.getRectWidth(mScaleImagePoints);
      float scaleFactorHeight =
              100f * mLoadedSampleSize / BitmapUtils.getRectHeight(mScaleImagePoints);
      mCropOverlayView.setCropWindowLimits(
              getWidth(), getHeight(), scaleFactorWidth, scaleFactorHeight);
    }

    // set the bitmap rectangle and update the crop window after scale factor is set
    mCropOverlayView.setBounds(clear ? null : mImagePoints, getWidth(), getHeight());
  }

  // endregion

  //=============================================================================
  // region: Внутренний класс: CropShape
  //=============================================================================

  /**
   * The possible cropping area shape.<br>
   * To set square/circle crop shape set aspect ratio to 1:1.
   */
  public enum CropShape {
    RECTANGLE,
    OVAL
  }

  // endregion

  //=============================================================================
  // region: Внутренний класс: ScaleType
  //=============================================================================

  /**
   * Options for scaling the bounds of cropping image to the bounds of Crop Image View.<br>
   * Note: Some options are affected by auto-zoom, if enabled.
   */
  public enum ScaleType {

    /**
     * Scale the image uniformly (maintain the image's aspect ratio) to fit in crop image view.<br>
     * The largest dimension will be equals to crop image view and the second dimension will be
     * smaller.
     */
    FIT_CENTER,

    /**
     * Center the image in the view, but perform no scaling.<br>
     * Note: If auto-zoom is enabled and the source image is smaller than crop image view then it
     * will be scaled uniformly to fit the crop image view.
     */
    CENTER,

    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
     * and height) of the image will be equal to or <b>larger</b> than the corresponding dimension
     * of the view (minus padding).<br>
     * The image is then centered in the view.
     */
    CENTER_CROP,

    /**
     * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
     * and height) of the image will be equal to or <b>less</b> than the corresponding dimension of
     * the view (minus padding).<br>
     * The image is then centered in the view.<br>
     * Note: If auto-zoom is enabled and the source image is smaller than crop image view then it
     * will be scaled uniformly to fit the crop image view.
     */
    CENTER_INSIDE
  }
  // endregion

  //=============================================================================
  // region: Внутренний класс: Guidelines
  //=============================================================================

  /** The possible guidelines showing types. */
  public enum Guidelines {
    /** Never show */
    OFF,

    /** Show when crop move action is live */
    ON_TOUCH,

    /** Always show */
    ON
  }
  // endregion

  //=============================================================================
  // region: Внутренний класс: RequestSizeOptions
  //=============================================================================

  /** Possible options for handling requested width/height for cropping. */
  public enum RequestSizeOptions {

    /** No resize/sampling is done unless required for memory management (OOM). */
    NONE,

    /**
     * Only sample the image during loading (if image set using URI) so the smallest of the image
     * dimensions will be between the requested size and x2 requested size.<br>
     * NOTE: resulting image will not be exactly requested width/height see: <a
     * href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html">Loading
     * Large Bitmaps Efficiently</a>.
     */
    SAMPLING,

    /**
     * Resize the image uniformly (maintain the image's aspect ratio) so that both dimensions (width
     * and height) of the image will be equal to or <b>less</b> than the corresponding requested
     * dimension.<br>
     * If the image is smaller than the requested size it will NOT change.
     */
    RESIZE_INSIDE,

    /**
     * Resize the image uniformly (maintain the image's aspect ratio) to fit in the given
     * width/height.<br>
     * The largest dimension will be equals to the requested and the second dimension will be
     * smaller.<br>
     * If the image is smaller than the requested size it will enlarge it.
     */
    RESIZE_FIT,

    /**
     * Resize the image to fit exactly in the given width/height.<br>
     * This resize method does NOT preserve aspect ratio.<br>
     * If the image is smaller than the requested size it will enlarge it.
     */
    RESIZE_EXACT
  }
  // endregion

  //=============================================================================
  // region: Внутренний класс: OnSetImageUriCompleteListener
  //=============================================================================

  /** Interface definition for a callback to be invoked when the crop overlay is released. */
  public interface OnSetCropOverlayReleasedListener {

    /**
     * Called when the crop overlay changed listener is called and inProgress is false.
     *
     * @param rect The rect coordinates of the cropped overlay
     */
    void onCropOverlayReleased(Rect rect);
  }
  // endregion

  //=============================================================================
  // region: Интерфейсы
  //=============================================================================

  /** Interface definition for a callback to be invoked when the crop overlay is released. */
  public interface OnSetCropOverlayMovedListener {

    /**
     * Called when the crop overlay is moved
     *
     * @param rect The rect coordinates of the cropped overlay
     */
    void onCropOverlayMoved(Rect rect);
  }

  /** Interface definition for a callback to be invoked when the crop overlay is released. */
  public interface OnSetCropWindowChangeListener {

    /** Called when the crop window is changed */
    void onCropWindowChanged();
  }

  /** Interface definition for a callback to be invoked when image async loading is complete. */
  public interface OnSetImageUriCompleteListener {

    /**
     * Called when a crop image view has completed loading image for cropping.<br>
     * If loading failed error parameter will contain the error.
     *
     * @param view The crop image view that loading of image was complete.
     * @param uri the URI of the image that was loading
     * @param error if error occurred during loading will contain the error, otherwise null.
     */
    void onSetImageUriComplete(CropImageView view, Uri uri, Exception error);
  }
  // endregion

  //=============================================================================
  // region: Внутренний класс: OnGetCroppedImageCompleteListener
  //=============================================================================

  /** Interface definition for a callback to be invoked when image async crop is complete. */
  public interface OnCropImageCompleteListener {

    /**
     * Called when a crop image view has completed cropping image.<br>
     * Result object contains the cropped bitmap, saved cropped image uri, crop points data or the
     * error occured during cropping.
     *
     * @param view The crop image view that cropping of image was complete.
     * @param result the crop image result data (with cropped image or error)
     */
    void onCropImageComplete(CropImageView view, CropResult result);
  }
  // endregion

  //=============================================================================
  // region: Внутренний класс: ActivityResult
  //=============================================================================

  /** Result data of crop image. */
  public static class CropResult {

    /**
     * The image bitmap of the original image loaded for cropping.<br>
     * Null if uri used to load image or activity result is used.
     */
    private final Bitmap mOriginalBitmap;

    /**
     * The Android uri of the original image loaded for cropping.<br>
     * Null if bitmap was used to load image.
     */
    private final Uri mOriginalUri;

    /**
     * The cropped image bitmap result.<br>
     * Null if save cropped image was executed, no output requested or failure.
     */
    private final Bitmap mBitmap;

    /**
     * The Android uri of the saved cropped image result.<br>
     * Null if get cropped image was executed, no output requested or failure.
     */
    private final Uri mUri;

    /** The error that failed the loading/cropping (null if successful) */
    private final Exception mError;

    /** The 4 points of the cropping window in the source image */
    private final float[] mCropPoints;

    /** The rectangle of the cropping window in the source image */
    private final Rect mCropRect;

    /** The rectangle of the source image dimensions */
    private final Rect mWholeImageRect;

    /** The final rotation of the cropped image relative to source */
    private final int mRotation;

    /** sample size used creating the crop bitmap to lower its size */
    private final int mSampleSize;

    CropResult(
            Bitmap originalBitmap,
            Uri originalUri,
            Bitmap bitmap,
            Uri uri,
            Exception error,
            float[] cropPoints,
            Rect cropRect,
            Rect wholeImageRect,
            int rotation,
            int sampleSize) {
      mOriginalBitmap = originalBitmap;
      mOriginalUri = originalUri;
      mBitmap = bitmap;
      mUri = uri;
      mError = error;
      mCropPoints = cropPoints;
      mCropRect = cropRect;
      mWholeImageRect = wholeImageRect;
      mRotation = rotation;
      mSampleSize = sampleSize;
    }

    /**
     * The image bitmap of the original image loaded for cropping.<br>
     * Null if uri used to load image or activity result is used.
     */
    public Bitmap getOriginalBitmap() {
      return mOriginalBitmap;
    }

    /**
     * The Android uri of the original image loaded for cropping.<br>
     * Null if bitmap was used to load image.
     */
    public Uri getOriginalUri() {
      return mOriginalUri;
    }

    /** Is the result is success or error. */
    public boolean isSuccessful() {
      return mError == null;
    }

    /**
     * The cropped image bitmap result.<br>
     * Null if save cropped image was executed, no output requested or failure.
     */
    public Bitmap getBitmap() {
      return mBitmap;
    }

    /**
     * The Android uri of the saved cropped image result Null if get cropped image was executed, no
     * output requested or failure.
     */
    public Uri getUri() {
      return mUri;
    }

    /** The error that failed the loading/cropping (null if successful) */
    public Exception getError() {
      return mError;
    }

    /** The 4 points of the cropping window in the source image */
    public float[] getCropPoints() {
      return mCropPoints;
    }

    /** The rectangle of the cropping window in the source image */
    public Rect getCropRect() {
      return mCropRect;
    }

    /** The rectangle of the source image dimensions */
    public Rect getWholeImageRect() {
      return mWholeImageRect;
    }

    /** The final rotation of the cropped image relative to source */
    public int getRotation() {
      return mRotation;
    }

    /** sample size used creating the crop bitmap to lower its size */
    public int getSampleSize() {
      return mSampleSize;
    }
  }

  // endregion

}
