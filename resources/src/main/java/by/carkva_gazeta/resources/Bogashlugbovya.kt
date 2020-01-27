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
import kotlinx.android.synthetic.main.bogasluzbovya.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


class Bogashlugbovya : AppCompatActivity(), View.OnTouchListener, DialogFontSize.DialogFontSizeListener, WebViewCustom.OnScrollChangedCallback, WebViewCustom.OnBottomListener, MyWebViewClient.OnLinkListenner {

    private val ulAnimationDelay = 300
    private val mHideHandler: Handler = Handler()
    @SuppressLint("InlinedApi")
    private val mHidePart2Runnable = Runnable {
        WebView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
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
    private var title: String = ""
    private var men = false
    private var scrollTimer: Timer = Timer()
    private var procentTimer: Timer = Timer()
    private var resetTimer: Timer = Timer()
    private lateinit var g: GregorianCalendar
    private var levo = false
    private var pravo = false
    private var niz = false
    private var positionY = 0
    private var menu = 1
    private var checkSetDzenNoch = false
    private var mActionDown = false
    private val orientation: Int
        get() {
            val rotation = windowManager.defaultDisplay.rotation
            val displayOrientation = resources.configuration.orientation
            if (displayOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                return if (rotation == Surface.ROTATION_270 || rotation == Surface.ROTATION_180) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_90) return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

    override fun onDialogFontSizePositiveClick() {
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        val webSettings = WebView.settings
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.setAppCacheEnabled(false)
        webSettings.blockNetworkImage = true
        webSettings.loadsImagesAutomatically = true
        webSettings.setGeolocationEnabled(false)
        webSettings.setNeedInitialFocus(false)
        webSettings.defaultFontSize = fontBiblia.toInt()
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

    @SuppressLint("ClickableViewAccessibility", "AddJavascriptInterface", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bogasluzbovya)
        autoscroll = k.getBoolean("autoscroll", false)
        spid = k.getInt("autoscrollSpid", 60)
        WebView.setOnTouchListener(this)
        //WebView.setOnLongClickListener { scrollTimer != null }
        val client = MyWebViewClient()
        client.setOnLinkListenner(this)
        WebView.webViewClient = client
        constraint.setOnTouchListener(this)
        if (dzenNoch) {
            progress.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
            WebView.setBackgroundColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorbackground_material_dark))
        }
        if (savedInstanceState != null) {
            fullscreenPage = savedInstanceState.getBoolean("fullscreen")
            if (savedInstanceState.getBoolean("seach")) {
                textSearch.visibility = View.VISIBLE
                textCount.visibility = View.VISIBLE
                imageView6.visibility = View.VISIBLE
                imageView5.visibility = View.VISIBLE
            }
            checkSetDzenNoch = savedInstanceState.getBoolean("checkSetDzenNoch")
        }
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
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
        WebView.visibility = View.VISIBLE
        val shlugbovya: Int = intent.extras?.getInt("bogashlugbovya", 0) ?: 0
        menu = intent.extras?.getInt("menu", 1) ?: 1
        var inputStream = resources.openRawResource(R.raw.bogashlugbovya1)
        when (menu) {
            1 -> {
                when (shlugbovya) {
                    0 -> {
                        resurs = "bogashlugbovya1"
                        title = "Боская Літургія між сьвятымі айца нашага Яна Залатавуснага"
                        inputStream = resources.openRawResource(R.raw.bogashlugbovya1)
                    }
                    1 -> {
                        resurs = "bogashlugbovya4"
                        title = "Набажэнства ў гонар Маці Божай Нястомнай Дапамогі"
                        inputStream = resources.openRawResource(R.raw.bogashlugbovya4)
                    }
                    3 -> {
                        resurs = "bogashlugbovya6"
                        title = "Ютрань нядзельная (у скароце)"
                        inputStream = resources.openRawResource(R.raw.bogashlugbovya6)
                    }
                    4 -> {
                        resurs = "bogashlugbovya8"
                        title = "Абедніца"
                        inputStream = resources.openRawResource(R.raw.bogashlugbovya8)
                    }
                    5 -> {
                        resurs = "bogashlugbovya11"
                        title = "Служба за памерлых — Малая паніхіда"
                        inputStream = resources.openRawResource(R.raw.bogashlugbovya11)
                    }
                }
            }
            2 -> {
                when (shlugbovya) {
                    0 -> {
                        resurs = "malitvy1"
                        title = getString(by.carkva_gazeta.malitounik.R.string.malitvy1)
                        inputStream = resources.openRawResource(R.raw.malitvy1)
                    }
                    1 -> {
                        resurs = "malitvy2"
                        title = getString(by.carkva_gazeta.malitounik.R.string.malitvy2)
                        inputStream = resources.openRawResource(R.raw.malitvy2)
                    }
                }
            }
            3 -> {
                when (shlugbovya) {
                    0 -> {
                        inputStream = resources.openRawResource(R.raw.akafist0)
                        resurs = "akafist0"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist0)
                    }
                    1 -> {
                        inputStream = resources.openRawResource(R.raw.akafist1)
                        resurs = "akafist1"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist1)
                    }
                    2 -> {
                        inputStream = resources.openRawResource(R.raw.akafist2)
                        resurs = "akafist2"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist2)
                    }
                    3 -> {
                        inputStream = resources.openRawResource(R.raw.akafist3)
                        resurs = "akafist3"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist3)
                    }
                    4 -> {
                        inputStream = resources.openRawResource(R.raw.akafist4)
                        resurs = "akafist4"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist4)
                    }
                    5 -> {
                        inputStream = resources.openRawResource(R.raw.akafist5)
                        resurs = "akafist5"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist5)
                    }
                    6 -> {
                        inputStream = resources.openRawResource(R.raw.akafist6)
                        resurs = "akafist6"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist6)
                    }
                    7 -> {
                        inputStream = resources.openRawResource(R.raw.akafist7)
                        resurs = "akafist7"
                        title = resources.getString(by.carkva_gazeta.malitounik.R.string.akafist7)
                    }
                }
            }
            4 -> {
                when (shlugbovya) {
                    0 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec0)
                        resurs = "ruzanec0"
                        title = "Малітвы на вяровіцы"
                    }
                    1 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec2)
                        resurs = "ruzanec2"
                        title = "Молімся на ружанцы"
                    }
                    2 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec1)
                        resurs = "ruzanec1"
                        title = "Разважаньні на Ружанец"
                    }
                    3 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec3)
                        resurs = "ruzanec3"
                        title = "Частка I. Радасныя таямніцы (пн, сб)"
                    }
                    4 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec4)
                        resurs = "ruzanec4"
                        title = "Частка II. Балесныя таямніцы (аўт, пт)"
                    }
                    5 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec5)
                        resurs = "ruzanec5"
                        title = "Частка III. Слаўныя таямніцы (ср, ндз)"
                    }
                    6 -> {
                        inputStream = resources.openRawResource(R.raw.ruzanec6)
                        resurs = "ruzanec6"
                        title = "Частка IV. Таямніцы сьвятла (чц)"
                    }
                }
            }
            //akafist.addJavascriptInterface(WebAppInterface(this, getSupportFragmentManager()), "Android")
        }
        positionY = (k.getInt(resurs + "Scroll", 0) / resources.displayMetrics.density).toInt()
        WebView.setOnScrollChangedCallback(this)
        WebView.setOnBottomListener(this)
        val webSettings = WebView.settings
        webSettings.standardFontFamily = "sans-serif-condensed"
        webSettings.defaultFontSize = fontBiblia.toInt()
        webSettings.javaScriptEnabled = true
        //akafist.addJavascriptInterface(WebAppInterface(this, getSupportFragmentManager()), "Android")
        scrollView2.visibility = View.GONE
        WebView.loadDataWithBaseURL("malitounik-app//carkva-gazeta.by/", loadData(inputStream), "text/html", "utf-8", null)
        men = VybranoeView.checkVybranoe(this, resurs)
        if (k.getBoolean("help_str", true)) {
            startActivity(Intent(this, HelpText::class.java))
            val prefEditor: Editor = k.edit()
            prefEditor.putBoolean("help_str", false)
            prefEditor.apply()
        }
        requestedOrientation = if (k.getBoolean("orientation", false)) {
            orientation
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

    private fun stopProcent() {
        procentTimer.cancel()
    }

    private fun startProcent() {
        g = Calendar.getInstance() as GregorianCalendar
        procentTimer = Timer()
        val procentSchedule = object : TimerTask() {
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
        procentTimer.schedule(procentSchedule, 20, 20)
    }

    private fun loadData(inputStream: InputStream): String {
        val builder = StringBuilder()
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
            if (menu == 1) {
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
                            t.printStackTrace()
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
                            t.printStackTrace()
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

    private fun stopAutoScroll() {
        scrollTimer.cancel()
        resetTimer = Timer()
        val resetSchedule: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }
        }
        resetTimer.schedule(resetSchedule, 60000)
    }

    private fun startAutoScroll() {
        resetTimer.cancel()
        scrollTimer = Timer()
        val scrollerSchedule = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (!mActionDown && !MainActivity.dialogVisable) {
                        WebView.scrollBy(0, 2)
                    }
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        scrollTimer.schedule(scrollerSchedule, spid.toLong(), spid.toLong())
    }

    @SuppressLint("SetTextI18n")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val heightConstraintLayout = constraint.height
        val widthConstraintLayout = constraint.width
        val otstup = (10 * resources.displayMetrics.density).toInt()
        val y = event.y.toInt()
        val x = event.x.toInt()
        val prefEditor: Editor = k.edit()
        if (v.id == R.id.WebView) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) menu.findItem(by.carkva_gazeta.malitounik.R.id.action_find).isVisible = true
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
            checkSetDzenNoch = true
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
                requestedOrientation = orientation
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
            men = VybranoeView.setVybranoe(this, resurs, title)
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
            if (checkSetDzenNoch) onSupportNavigateUp() else super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        val prefEditor = k.edit()
        prefEditor.putInt(resurs + "Scroll", positionY)
        prefEditor.apply()
        stopAutoScroll()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        scrollTimer.cancel()
        resetTimer.cancel()
        procentTimer.cancel()
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
        WebView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, ulAnimationDelay.toLong())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("fullscreen", fullscreenPage)
        outState.putBoolean("checkSetDzenNoch", checkSetDzenNoch)
        if (textSearch.visibility == View.VISIBLE) outState.putBoolean("seach", true) else outState.putBoolean("seach", false)
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