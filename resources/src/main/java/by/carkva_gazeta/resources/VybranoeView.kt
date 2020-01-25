package by.carkva_gazeta.resources

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.malitounik.InteractiveScrollView.OnBottomReachedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.bogasluzbovya.*
import java.io.*
import java.lang.reflect.Field
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


class VybranoeView : AppCompatActivity(), View.OnTouchListener, DialogFontSize.DialogFontSizeListener, WebViewCustom.OnScrollChangedCallback, WebViewCustom.OnBottomListener, InteractiveScrollView.OnScrollChangedCallback, MyWebViewClient.OnLinkListenner {

    private val ulAnimationDelay = 300
    private val mHideHandler: Handler = Handler()
    @SuppressLint("InlinedApi")
    private val mHidePart2Runnable = Runnable {
        scrollView2.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
    private val mShowPart2Runnable = Runnable {
        supportActionBar?.show()
    }

    private var fullscreenPage = false
    private lateinit var k: SharedPreferences
    private var fontBiblia = SettingsActivity.GET_DEFAULT_FONT_SIZE
    private var dzenNoch = false
    private var autoscroll = false
    private var n = 0
    private var yS = 0
    private var spid = 60
    private var resurs: String = ""
    private var men = true
    private var scrollTimer: Timer? = null
    private var procentTimer: Timer? = null
    private var resetTimer: Timer? = null
    private var scrollerSchedule: TimerTask? = null
    private var procentSchedule: TimerTask? = null
    private lateinit var g: GregorianCalendar
    private var levo = false
    private var pravo = false
    private var niz = false
    private var positionY = 0
    private var title: String = ""
    private var editVybranoe = false
    private var mActionDown = false

    companion object {
        fun setVybranoe(context: Context, resurs: String, title: String): Boolean {
            val gson = Gson()
            val file = File(context.filesDir.toString() + "/Vybranoe.json")
            if (file.exists()) {
                MenuVybranoe.vybranoe = try {
                    val type: Type = object : TypeToken<ArrayList<VybranoeData?>?>() {}.type
                    gson.fromJson(file.readText(), type)
                } catch (t: Throwable) {
                    file.delete()
                    ArrayList()
                }
            }
            var check = true
            val fields = R.raw::class.java.fields
            for (field in fields) {
                if (field.name.intern() == resurs) {
                    for (i in 0 until MenuVybranoe.vybranoe.size) {
                        if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) {
                            MenuVybranoe.vybranoe.removeAt(i)
                            check = false
                            break
                        }
                    }
                    break
                }
            }
            val fields2 = by.carkva_gazeta.malitounik.R.raw::class.java.fields
            for (field in fields2) {
                if (field.name.intern() == resurs) {
                    for (i in 0 until MenuVybranoe.vybranoe.size) {
                        if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) {
                            MenuVybranoe.vybranoe.removeAt(i)
                            check = false
                            break
                        }
                    }
                    break
                }
            }
            if (check) {
                MenuVybranoe.vybranoe.add(VybranoeData(resurs, title))
            }
            val outputStream = FileWriter(file)
            outputStream.write(gson.toJson(MenuVybranoe.vybranoe))
            outputStream.close()
            return check
        }

        fun checkVybranoe(context: Context, resurs: String): Boolean {
            var check = false
            val gson = Gson()
            val file = File(context.filesDir.toString() + "/Vybranoe.json")
            if (file.exists()) {
                try {
                    val type: Type = object : TypeToken<ArrayList<VybranoeData?>?>() {}.type
                    MenuVybranoe.vybranoe = gson.fromJson(file.readText(), type)
                } catch (t: Throwable) {
                    file.delete()
                    return false
                }
            } else {
                return false
            }
            val fields: Array<Field> = R.raw::class.java.fields
            for (field in fields) {
                if (field.name.intern() == resurs) {
                    for (i in 0 until MenuVybranoe.vybranoe.size) {
                        if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) { //MenuVybranoe.vybranoe.remove(i)
                            check = true
                            break
                        }
                    }
                    break
                }
            }
            val fields2: Array<Field> = by.carkva_gazeta.malitounik.R.raw::class.java.fields
            for (field in fields2) {
                if (field.name.intern() == resurs) {
                    for (i in 0 until MenuVybranoe.vybranoe.size) {
                        if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) { //MenuVybranoe.vybranoe.remove(i)
                            check = true
                            break
                        }
                    }
                    break
                }
            }
            return check
        }
    }

    private fun getOrientation(): Int {
        val rotation = windowManager.defaultDisplay.rotation
        val displayOrientation = resources.configuration.orientation
        if (displayOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_180) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_90) return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onDialogFontSizePositiveClick() {
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        if (TextView.visibility == View.VISIBLE) {
            TextView.textSize = fontBiblia
        } else {
            val webSettings = WebView.settings
            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
            webSettings.setAppCacheEnabled(false)
            webSettings.blockNetworkImage = true
            webSettings.loadsImagesAutomatically = true
            webSettings.setGeolocationEnabled(false)
            webSettings.setNeedInitialFocus(false)
            webSettings.defaultFontSize = fontBiblia.toInt()
        }
    }

    override fun onScroll(t: Int) {
        positionY = t
    }

    override fun onBottom() {
        stopAutoScroll()
        val prefEditors = k.edit()
        prefEditors.putBoolean("autoscroll", false)
        prefEditors.apply()
        invalidateOptionsMenu()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        dzenNoch = k.getBoolean("dzen_noch", false)
        super.onCreate(savedInstanceState)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        setContentView(R.layout.bogasluzbovya)
        autoscroll = k.getBoolean("autoscroll", false)
        spid = k.getInt("autoscrollSpid", 60)
        WebView.setOnTouchListener(this)
        WebView.setOnLongClickListener { scrollTimer != null }
        val client = MyWebViewClient()
        client.setOnLinkListenner(this)
        WebView.webViewClient = client
        scrollView2.setOnTouchListener(this)
        scrollView2.setOnScrollChangedCallback(this)
        scrollView2.setOnLongClickListener { scrollTimer != null }
        constraint.setOnTouchListener(this)
        autoscroll = k.getBoolean("autoscroll", false)
        scrollView2.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached() {
                stopAutoScroll()
                val prefEditors = k.edit()
                prefEditors.putBoolean("autoscroll", false)
                prefEditors.apply()
                invalidateOptionsMenu()
            }
        }
        )
        if (savedInstanceState != null) {
            fullscreenPage = savedInstanceState.getBoolean("fullscreen")
            editVybranoe = savedInstanceState.getBoolean("editVybranoe")
            if (savedInstanceState.getBoolean("seach")) {
                textSearch.visibility = View.VISIBLE
                textCount.visibility = View.VISIBLE
                imageView6.visibility = View.VISIBLE
                imageView5.visibility = View.VISIBLE
            }
        }
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        TextView.textSize = fontBiblia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (dzenNoch) {
                window.statusBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
                window.navigationBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimaryDark)
                window.navigationBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimaryDark)
            }
        }
        if (dzenNoch) {
            TextView.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorIcons))
            progress.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
        }
        resurs = intent?.getStringExtra("resurs") ?: ""
        if (resurs.contains("pesny")) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        title = intent?.getStringExtra("title") ?: ""
        val webSettings = WebView.settings
        webSettings.standardFontFamily = "sans-serif-condensed"
        webSettings.defaultFontSize = fontBiblia.toInt()
        webSettings.javaScriptEnabled = true
        //akafist.addJavascriptInterface(WebAppInterface(this, getSupportFragmentManager()), "Android")
        positionY = (k.getInt(resurs + "Scroll", 0) / resources.displayMetrics.density).toInt()
        WebView.setOnScrollChangedCallback(this)
        WebView.setOnBottomListener(this)
        if (resurs.intern().contains("bogashlugbovya") || resurs.intern().contains("akafist") || resurs.intern().contains("malitvy") || resurs.intern().contains("ruzanec") || resurs.intern().contains("ton")) {
            TextView.visibility = View.GONE
            WebView.visibility = View.VISIBLE
            WebView.loadDataWithBaseURL("malitounikApp-app//carkva-gazeta.by/", loadData(), "text/html", "utf-8", null)
        } else {
            WebView.visibility = View.GONE
            TextView.text = MainActivity.fromHtml(loadData())
            positionY = k.getInt(resurs + "Scroll", 0)
            scrollView2.post { scrollView2.scrollBy(0, positionY) }
        }
        if (k.getBoolean("help_str", true)) {
            startActivity(Intent(this, HelpText::class.java))
            val prefEditor: Editor = k.edit()
            prefEditor.putBoolean("help_str", false)
            prefEditor.apply()
        }
        requestedOrientation = if (k.getBoolean("orientation", false)) {
            getOrientation()
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun setTollbarTheme() {
        title_toolbar.setOnClickListener {
            title_toolbar.setHorizontallyScrolling(true)
            title_toolbar.freezesText = true
            title_toolbar.marqueeRepeatLimit = -1
            if (title_toolbar.isSelected) {
                title_toolbar.ellipsize = TextUtils.TruncateAt.END
                title_toolbar.isSelected = false
            } else {
                title_toolbar.ellipsize = TextUtils.TruncateAt.MARQUEE
                title_toolbar.isSelected = true
            }
        }
        title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title_toolbar.text = title
        if (dzenNoch) {
            toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
            toolbar.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
        }
    }

    private fun scrollWebView(): StringBuilder? {
        val script = StringBuilder()
        script.append("<script language=\"javascript\" type=\"text/javascript\">")
        script.append("\n")
        script.append("    function toY(){")
        script.append("\n")
        script.append("        window.scrollTo(0, ").append(positionY).append(")")
        script.append("\n")
        script.append("    }")
        script.append("\n")
        script.append("</script>")
        script.append("\n")
        return script
    }

    private fun loadData(): String {
        val builder = StringBuilder()
        var id = R.raw.bogashlugbovya1
        val fields = R.raw::class.java.fields
        for (field in fields) {
            if (field.name.intern() == resurs) {
                id = field.getInt(null)
                break
            }
        }
        val fields2 = by.carkva_gazeta.malitounik.R.raw::class.java.fields
        for (field in fields2) {
            if (field.name.intern() == resurs) {
                id = field.getInt(null)
                break
            }
        }
        val inputStream: InputStream = resources.openRawResource(id)
        val zmenyiaChastki = ZmenyiaChastki(this)
        val gregorian = Calendar.getInstance() as GregorianCalendar
        val dayOfWeek = gregorian.get(Calendar.DAY_OF_WEEK)
        val isr = InputStreamReader(inputStream)
        val reader = BufferedReader(isr)
        val color: String = if (dzenNoch) "<font color=\"#f44336\">"
        else "<font color=\"#d00505\">"
        reader.forEachLine {
            var line = it
            if (dzenNoch)
                line = line.replace("#d00505", "#f44336")
            line = line.replace("<head>", "<head>" + scrollWebView())
            line = line.replace("<body>", "<body onload='toY()'>")
            line = if (dzenNoch)
                line.replace("<html><head>", "<html><head><style type=\"text/css\">::selection {background: #eb9b9a} body{-webkit-tap-highlight-color: rgba(244,67,54,0.2); color: #fff; background-color: #303030; margin: 0; padding: 0}</style>")
            else
                line.replace("<html><head>", "<html><head><style type=\"text/css\">::selection {background: #eb9b9a} body{-webkit-tap-highlight-color: rgba(208,5,5,0.1); margin: 0; padding: 0}</style>")
            if (resurs.contains("bogashlugbovya")) {
                if (line.intern().contains("<KANDAK></KANDAK>")) {
                    line = line.replace("<KANDAK></KANDAK>", "")
                    builder.append(line)
                    try {
                        if (dayOfWeek == 1) {
                            builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(1))
                        } else {
                            builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 1))
                        }
                    } catch (t: Throwable) {
                        builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                    }
                }
                if (line.intern().contains("<PRAKIMEN></PRAKIMEN>")) {
                    line = line.replace("<PRAKIMEN></PRAKIMEN>", "")
                    builder.append(line)
                    try {
                        if (dayOfWeek == 1) {
                            builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(2))
                        } else {
                            builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 2))
                        }
                    } catch (t: Throwable) {
                        builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                    }
                }
                if (line.intern().contains("<ALILUIA></ALILUIA>")) {
                    line = line.replace("<ALILUIA></ALILUIA>", "")
                    builder.append(line)
                    try {
                        if (dayOfWeek == 1) {
                            builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(3))
                        } else {
                            builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 3))
                        }
                    } catch (t: Throwable) {
                        builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                    }
                }
                if (line.intern().contains("<PRICHASNIK></PRICHASNIK>")) {
                    line = line.replace("<PRICHASNIK></PRICHASNIK>", "")
                    builder.append(line)
                    try {
                        if (dayOfWeek == 1) {
                            builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(4))
                        } else {
                            builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 4))
                        }
                    } catch (t: Throwable) {
                        builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                    }
                }
                when {
                    line.intern().contains("<APCH></APCH>") -> {
                        line = line.replace("<APCH></APCH>", "")
                        var sv = zmenyiaChastki.sviatyia()
                        if (sv != "") {
                            val s1 = sv.split(":").toTypedArray()
                            val s2 = s1[1].split(";").toTypedArray()
                            sv = s1[0] + ":" + s2[0]
                            builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=8\">").append(color).append(sv).append("</font></a>").append("<br><br>\n")
                        } else builder.append(line)
                        try {
                            builder.append(zmenyiaChastki.zmenya(1))
                        } catch (t: Throwable) {
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                        }
                    }
                    line.intern().contains("<EVCH></EVCH>") -> {
                        line = line.replace("<EVCH></EVCH>", "")
                        var sv = zmenyiaChastki.sviatyia()
                        if (sv != "") {
                            val s1 = sv.split(":").toTypedArray()
                            val s2 = s1[1].split(";").toTypedArray()
                            sv = s1[0] + ":" + s2[1]
                            builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=9\">").append(color).append(sv).append("</font></a>").append("<br><br>\n")
                        } else builder.append(line)
                        try {
                            builder.append(zmenyiaChastki.zmenya(0))
                        } catch (t: Throwable) {
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.chteniaErr)).append("<br>\n")
                        }
                    }
                    else -> {
                        builder.append(line)
                    }
                }
            } else {
                builder.append(line)
            }
        }
        inputStream.close()
        // API >= 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            WebView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                if (numberOfMatches == 0)
                    textCount.setText(by.carkva_gazeta.malitounik.R.string.niama)
                else
                    textCount.text = (activeMatchOrdinal + 1).toString().plus(" ($numberOfMatches)")
            }
            if (dzenNoch)
                imageView6.setImageResource(by.carkva_gazeta.malitounik.R.drawable.up_black)
            imageView6.setOnClickListener { WebView.findNext(false) }
            textSearch.addTextChangedListener(object : TextWatcher {
                var editPosition = 0
                var check = 0
                var editch = true

                override fun afterTextChanged(s: Editable?) {
                    var edit = s.toString()
                    if (editch) {
                        edit = edit.replace("и", "і")
                        edit = edit.replace("щ", "ў")
                        edit = edit.replace("ъ", "'")
                        edit = edit.replace("И", "І")
                        edit = edit.replace("Щ", "Ў")
                        edit = edit.replace("Ъ", "'")
                        if (check != 0) {
                            textSearch.removeTextChangedListener(this)
                            textSearch.setText(edit)
                            textSearch.setSelection(editPosition)
                            textSearch.addTextChangedListener(this)
                        }
                    }
                    WebView.findAllAsync(edit)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    editch = count != after
                    check = after
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    editPosition = start + count
                }
            })
            if (dzenNoch)
                imageView5.setImageResource(by.carkva_gazeta.malitounik.R.drawable.niz_back)
            imageView5.setOnClickListener { WebView.findNext(true) }
        }
        return builder.toString()
    }

    private fun stopProcent() {
        if (procentTimer != null) {
            procentTimer?.cancel()
            procentTimer = null
        }
        procentSchedule = null
    }

    private fun startProcent() {
        g = Calendar.getInstance() as GregorianCalendar
        if (procentTimer == null) {
            procentTimer = Timer()
            if (procentSchedule != null) {
                procentSchedule?.cancel()
                procentSchedule = null
            }
            procentSchedule = object : TimerTask() {
                override fun run() {
                    val g2: GregorianCalendar = Calendar.getInstance() as GregorianCalendar
                    if (g.timeInMillis + 1000 <= g2.timeInMillis) {
                        runOnUiThread {
                            progress.visibility = View.GONE
                            stopProcent()
                        }
                    }
                }
            }
            procentTimer?.schedule(procentSchedule, 20, 20)
        }
    }

    private fun stopAutoScroll() {
        if (scrollTimer != null) {
            scrollTimer?.cancel()
            scrollTimer = null
        }
        scrollerSchedule = null
        if (resetTimer == null) {
            resetTimer = Timer()
            val resetSchedule: TimerTask = object : TimerTask() {
                override fun run() {
                    runOnUiThread { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                }
            }
            resetTimer?.schedule(resetSchedule, 60000)
        }
    }

    private fun startAutoScroll() {
        if (resetTimer != null) {
            resetTimer?.cancel()
            resetTimer = null
        }
        if (scrollTimer == null) {
            scrollTimer = Timer()
            if (scrollerSchedule != null) {
                scrollerSchedule?.cancel()
                scrollerSchedule = null
            }
            scrollerSchedule = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        if (!mActionDown && !MainActivity.dialogVisable) {
                            WebView.scrollBy(0, 2)
                        }
                    }
                }
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            scrollTimer?.schedule(scrollerSchedule, spid.toLong(), spid.toLong())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val heightConstraintLayout = constraint.height
        val widthConstraintLayout = constraint.width
        val otstup = (10 * resources.displayMetrics.density).toInt()
        val y = event.y.toInt()
        val x = event.x.toInt()
        val prefEditor: Editor = k.edit()
        if (v.id == R.id.WebView || v.id == by.carkva_gazeta.malitounik.R.id.scrollView2) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mActionDown = true
                MotionEvent.ACTION_UP -> mActionDown = false
                MotionEvent.ACTION_MOVE -> {
                    val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(textSearch.windowToken, 0)
                }
            }
            return false
        }
        if (v.id == R.id.constraint) {
            if (MainActivity.checkBrightness) {
                MainActivity.brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) * 100 / 255
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    n = event.y.toInt()
                    yS = event.x.toInt()
                    val proc: Int
                    if (x < otstup) {
                        levo = true
                        progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                        progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                        progress.visibility = View.VISIBLE
                    }
                    if (x > widthConstraintLayout - otstup) {
                        pravo = true
                        var minmax = ""
                        if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MIN) minmax = " (мін)"
                        if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MAX) minmax = " (макс)"
                        progress.text = "$fontBiblia sp$minmax"
                        progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
                        progress.visibility = View.VISIBLE
                    }
                    if (y > heightConstraintLayout - otstup) {
                        niz = true
                        spid = k.getInt("autoscrollSpid", 60)
                        proc = 100 - (spid - 15) * 100 / 215
                        progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                        progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                        progress.visibility = View.VISIBLE
                        startProcent()
                        autoscroll = k.getBoolean("autoscroll", false)
                        if (!autoscroll) {
                            startAutoScroll()
                            prefEditor.putBoolean("autoscroll", true)
                            prefEditor.apply()
                            invalidateOptionsMenu()
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (x < otstup && y > n && y % 15 == 0) {
                        if (MainActivity.brightness > 0) {
                            MainActivity.brightness = MainActivity.brightness - 1
                            val lp = window.attributes
                            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
                            window.attributes = lp
                            progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                            MainActivity.checkBrightness = false
                        }
                    }
                    if (x < otstup && y < n && y % 15 == 0) {
                        if (MainActivity.brightness < 100) {
                            MainActivity.brightness = MainActivity.brightness + 1
                            val lp = window.attributes
                            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
                            window.attributes = lp
                            progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                            MainActivity.checkBrightness = false
                        }
                    }
                    if (x > widthConstraintLayout - otstup && y > n && y % 26 == 0) {
                        if (fontBiblia > SettingsActivity.GET_FONT_SIZE_MIN) {
                            fontBiblia -= 4
                            prefEditor.putFloat("font_biblia", fontBiblia)
                            prefEditor.apply()
                            val webSettings = WebView.settings
                            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
                            webSettings.setAppCacheEnabled(false)
                            webSettings.blockNetworkImage = true
                            webSettings.loadsImagesAutomatically = true
                            webSettings.setGeolocationEnabled(false)
                            webSettings.setNeedInitialFocus(false)
                            webSettings.defaultFontSize = fontBiblia.toInt()
                            var min = ""
                            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MIN) min = " (мін)"
                            progress.text = "$fontBiblia sp$min"
                            progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
                        }
                    }
                    if (x > widthConstraintLayout - otstup && y < n && y % 26 == 0) {
                        if (fontBiblia < SettingsActivity.GET_FONT_SIZE_MAX) {
                            fontBiblia += 4
                            prefEditor.putFloat("font_biblia", fontBiblia)
                            prefEditor.apply()
                            val webSettings = WebView.settings
                            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
                            webSettings.setAppCacheEnabled(false)
                            webSettings.blockNetworkImage = true
                            webSettings.loadsImagesAutomatically = true
                            webSettings.setGeolocationEnabled(false)
                            webSettings.setNeedInitialFocus(false)
                            webSettings.defaultFontSize = fontBiblia.toInt()
                            var max = ""
                            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MAX) max = " (макс)"
                            progress.text = "$fontBiblia sp$max"
                            progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
                        }
                    }
                    if (y > heightConstraintLayout - otstup && x > yS && x % 25 == 0) {
                        if (spid in 20..235) {
                            spid -= 5
                            val proc = 100 - (spid - 15) * 100 / 215
                            progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                            progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                            progress.visibility = View.VISIBLE
                            startProcent()
                            stopAutoScroll()
                            startAutoScroll()
                        }
                    }
                    if (y > heightConstraintLayout - otstup && x < yS && x % 25 == 0) {
                        if (spid in 10..225) {
                            spid += 5
                            val proc = 100 - (spid - 15) * 100 / 215
                            progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                            progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                            progress.visibility = View.VISIBLE
                            startProcent()
                            stopAutoScroll()
                            startAutoScroll()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    if (levo) {
                        levo = false
                        progress.visibility = View.GONE
                    }
                    if (pravo) {
                        pravo = false
                        progress.visibility = View.GONE
                    }
                    if (niz) {
                        niz = false
                        prefEditor.putInt("autoscrollSpid", spid)
                        prefEditor.apply()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (levo) {
                        levo = false
                        progress.visibility = View.GONE
                    }
                    if (pravo) {
                        pravo = false
                        progress.visibility = View.GONE
                    }
                    if (niz) {
                        niz = false
                        prefEditor.putInt("autoscrollSpid", spid)
                        prefEditor.apply()
                    }
                }
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        autoscroll = k.getBoolean("autoscroll", false)
        if (resurs.contains("bogashlugbovya") || resurs.intern().contains("akafist") || resurs.intern().contains("malitvy") || resurs.intern().contains("ruzanec")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_find).isVisible = true
        }
        if (autoscroll) {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_plus).isVisible = true
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_minus).isVisible = true
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_auto).title = resources.getString(by.carkva_gazeta.malitounik.R.string.autoScrolloff)
        } else {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_plus).isVisible = false
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_minus).isVisible = false
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_auto).title = resources.getString(by.carkva_gazeta.malitounik.R.string.autoScrollon)
        }
        if (men) {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_on)
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe_del)
        } else {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_off)
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe)
        }
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_orientation).isChecked = k.getBoolean("orientation", false)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_dzen_noch).isChecked = k.getBoolean("dzen_noch", false)
        val item: MenuItem = menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe)
        val spanString = SpannableString(menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).title.toString())
        val end = spanString.length
        spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        item.title = spanString
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(by.carkva_gazeta.malitounik.R.menu.akafist, menu)
        for (i in 0 until menu.size()) {
            val item: MenuItem = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        dzenNoch = k.getBoolean("dzen_noch", false)
        val prefEditor: Editor = k.edit()
        val id: Int = item.itemId
        if (id == by.carkva_gazeta.malitounik.R.id.action_dzen_noch) {
            editVybranoe = true
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                prefEditor.putBoolean("dzen_noch", true)
            } else {
                prefEditor.putBoolean("dzen_noch", false)
            }
            prefEditor.apply()
            recreate()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_find) {
            textSearch.visibility = View.VISIBLE
            textCount.visibility = View.VISIBLE
            imageView6.visibility = View.VISIBLE
            imageView5.visibility = View.VISIBLE
            textSearch.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_orientation) {
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                requestedOrientation = getOrientation()
                prefEditor.putBoolean("orientation", true)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                prefEditor.putBoolean("orientation", false)
            }
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_plus) {
            if (spid in 20..235) {
                spid -= 5
                val proc = 100 - (spid - 15) * 100 / 215
                progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                progress.visibility = View.VISIBLE
                startProcent()
                stopAutoScroll()
                startAutoScroll()
                val prefEditors = k.edit()
                prefEditors.putInt("autoscrollSpid", spid)
                prefEditors.apply()
            }
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_minus) {
            if (spid in 10..225) {
                spid += 5
                val proc = 100 - (spid - 15) * 100 / 215
                progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                progress.visibility = View.VISIBLE
                startProcent()
                stopAutoScroll()
                startAutoScroll()
                val prefEditors = k.edit()
                prefEditors.putInt("autoscrollSpid", spid)
                prefEditors.apply()
            }
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_auto) {
            autoscroll = k.getBoolean("autoscroll", false)
            if (autoscroll) {
                stopAutoScroll()
                prefEditor.putBoolean("autoscroll", false)
            } else {
                startAutoScroll()
                prefEditor.putBoolean("autoscroll", true)
            }
            invalidateOptionsMenu()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_vybranoe) {
            editVybranoe = true
            men = setVybranoe(this, resurs, title)
            if (men) {
                val layout = LinearLayout(this)
                layout.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorPrimary)
                val density = resources.displayMetrics.density
                val realpadding = (10 * density).toInt()
                val toast = TextViewRobotoCondensed(this)
                toast.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorIcons))
                toast.setPadding(realpadding, realpadding, realpadding, realpadding)
                toast.text = getString(by.carkva_gazeta.malitounik.R.string.addVybranoe)
                toast.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN - 2)
                layout.addView(toast)
                val mes = Toast(this)
                mes.duration = Toast.LENGTH_SHORT
                mes.view = layout
                mes.show()
            }
            invalidateOptionsMenu()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_font) {
            val dialogFontSize = DialogFontSize()
            dialogFontSize.show(supportFragmentManager, "font")
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_bright) {
            val dialogBrightness = DialogBrightness()
            dialogBrightness.show(supportFragmentManager, "brightness")
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_fullscreen) {
            if (k.getBoolean("FullscreenHelp", true)) {
                val dialogHelpFullscreen = DialogHelpFullscreen()
                dialogHelpFullscreen.show(supportFragmentManager, "FullscreenHelp")
            }
            fullscreenPage = true
            hide()
        }
        prefEditor.apply()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val prefEditor = k.edit()
        prefEditor.putInt(resurs + "Scroll", positionY)
        prefEditor.apply()
        if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else if (textSearch.visibility == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textSearch.visibility = View.GONE
                textCount.visibility = View.GONE
                imageView6.visibility = View.GONE
                imageView5.visibility = View.GONE
                WebView.findAllAsync("")
                textSearch.setText("")
                textCount.setText(by.carkva_gazeta.malitounik.R.string.niama)
                val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(textSearch.windowToken, 0)
            }
        } else {
            if (editVybranoe)
                onSupportNavigateUp()
            else
                super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        setTollbarTheme()
        if (fullscreenPage) hide()
        autoscroll = k.getBoolean("autoscroll", false)
        spid = k.getInt("autoscrollSpid", 60)
        if (autoscroll) {
            startAutoScroll()
        }
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
    }

    private fun hide() {
        supportActionBar?.hide()
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, ulAnimationDelay.toLong())
    }

    private fun show() {
        scrollView2.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, ulAnimationDelay.toLong())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("fullscreen", fullscreenPage)
        outState.putBoolean("editVybranoe", editVybranoe)
        if (textSearch.visibility == View.VISIBLE)
            outState.putBoolean("seach", true)
        else
            outState.putBoolean("seach", false)
    }

    override fun onActivityStart() {
        val intent = Intent(this, MalitvyPasliaPrychascia::class.java)
        startActivity(intent)
        positionY = 0
    }

    override fun onDialogStart(message: String?) {
        val dialogLiturgia: DialogLiturgia = DialogLiturgia.getInstance(message)
        dialogLiturgia.show(supportFragmentManager, "dialog_liturgia")
    }
}
