package me.jbusdriver.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArrayMap
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.orhanobut.logger.Logger
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Administrator on 2017/4/8.
 */
typealias  KLog = Logger
enum class SizeUnit {
    Byte,
    KB,
    MB,
    GB,
    TB,
    Auto
}

const val KB = 1024.0
const val MB = KB * 1024
const val GB = MB * 1024
const val TB = GB * 1024

fun Long.formatFileSize(unit: SizeUnit = SizeUnit.Auto): String {
    if (this < 0) {
        return "未知大小"
    }
    var measureType = unit

    if (unit == SizeUnit.Auto) {
        measureType = if (this < KB) {
            SizeUnit.Byte
        } else if (this < MB) {
            SizeUnit.KB
        } else if (this < GB) {
            SizeUnit.MB
        } else if (this < TB) {
            SizeUnit.GB
        } else {
            SizeUnit.TB
        }
    }

    when (measureType) {
        SizeUnit.Byte -> return this.toString() + "B"
        SizeUnit.KB -> return String.format(Locale.US, "%.2fKB", this / KB)
        SizeUnit.MB -> return String.format(Locale.US, "%.2fMB", this / MB)
        SizeUnit.GB -> return String.format(Locale.US, "%.2fGB", this / GB)
        SizeUnit.TB -> return String.format(Locale.US, "%.2fPB", this / TB)
        else -> return this.toString() + "B"
    }
}

/*array map*/

fun <K, V> arrayMapof(vararg pairs: Pair<K, V>): ArrayMap<K, V> = ArrayMap<K, V>(pairs.size).apply { putAll(pairs) }
fun <K, V> arrayMapof(): ArrayMap<K, V> = ArrayMap()


/*Context*/
val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.displayMetrics: DisplayMetrics
    get() = resources.displayMetrics


fun Context.dpToPx(dp: Float): Int {
    return (dp * this.displayMetrics.density + 0.5).toInt()
}

fun Context.pxToDp(px: Float): Int {
    return (px / this.displayMetrics.density + 0.5).toInt()
}

fun Context.toast(str: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, str, duration).show()
}

private fun inflateView(context: Context, layoutResId: Int, parent: ViewGroup?,
                        attachToRoot: Boolean): View =
        LayoutInflater.from(context).inflate(layoutResId, parent, attachToRoot)

fun Context.inflate(layoutResId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View =
        inflateView(this, layoutResId, parent, attachToRoot)

/*gson*/
inline @Nullable fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: JsonElement) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: Reader) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)
//inline fun <reified T> Gson.fromJson(json: JsonReader) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

fun Any?.toJsonString() = AppContext.gson.toJson(this)

/*http*/
fun <R> Flowable<R>.addUserCase(sec: Int = 12) =
        this.timeout(sec.toLong(), TimeUnit.SECONDS, Schedulers.io()) //超时
                .subscribeOn(Schedulers.io())
                .take(1)


/*webview load */


/*view*/
fun View.measureIfNotMeasure() {
    if (this.measuredHeight != 0 || this.measuredWidth != 0) return
    this.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
}

val Context.scressWidth: Int
    inline get() {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

/**
 * 实现文本复制功能
 * add by wangqianzhou
 * @param content
 */
fun Context.copy(content: String) {
    // 得到剪贴板管理器
    val cmb = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cmb.primaryClip = ClipData.newPlainText(null, content)
}

/**
 * 实现粘贴功能
 * add by wangqianzhou
 * @param context
 * *
 * @return
 */
fun Context.paste(): String? {
    // 得到剪贴板管理器
    val cmb = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cmb.primaryClip?.let {
        if (it.itemCount > 0) it.getItemAt(0).coerceToText(this)?.toString() else null
    }
}


//string url -> get url host
val String.urlHost: String
    inline get() = Uri.parse(this).let {
        checkNotNull(it)
        KLog.d("uri : ${it.scheme} : ${it.host} : ${it.path}")
        "${it.scheme}://${it.host}"
    }