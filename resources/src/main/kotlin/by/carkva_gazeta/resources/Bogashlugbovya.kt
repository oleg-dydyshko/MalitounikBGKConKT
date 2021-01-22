package by.carkva_gazeta.resources

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.resources.databinding.BogasluzbovyaBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Runnable
import java.util.*

class Bogashlugbovya : AppCompatActivity(), View.OnTouchListener, DialogFontSize.DialogFontSizeListener, WebViewCustom.OnScrollChangedCallback, WebViewCustom.OnBottomListener, InteractiveScrollView.OnScrollChangedCallback, MyWebViewClient.OnLinkListenner {

    private val ulAnimationDelay = 300
    private val mHideHandler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private val mHidePart2Runnable = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
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
    private var resurs = ""
    private var men = true
    private var levo = false
    private var pravo = false
    private var niz = false
    private var positionY = 0
    private var title = ""
    private var editVybranoe = false
    private var mActionDown = false
    private var mAutoScroll = true
    private val orientation: Int
        get() {
            return MainActivity.getOrientation(this)
        }
    private lateinit var binding: BogasluzbovyaBinding
    private var autoScrollJob: Job? = null
    private var autoStartScrollJob: Job? = null
    private var procentJob: Job? = null
    private var resetTollbarJob: Job? = null

    companion object {
        private val resursMap = ArrayMap<String, Int>()

        init {
            resursMap["bogashlugbovya1"] = R.raw.bogashlugbovya1
            resursMap["bogashlugbovya4"] = R.raw.bogashlugbovya4
            resursMap["bogashlugbovya6"] = R.raw.bogashlugbovya6
            resursMap["bogashlugbovya8"] = R.raw.bogashlugbovya8
            resursMap["bogashlugbovya11"] = R.raw.bogashlugbovya11
            resursMap["bogashlugbovya12_1"] = R.raw.bogashlugbovya12_1
            resursMap["bogashlugbovya12_2"] = R.raw.bogashlugbovya12_2
            resursMap["bogashlugbovya12_3"] = R.raw.bogashlugbovya12_3
            resursMap["bogashlugbovya12_4"] = R.raw.bogashlugbovya12_4
            resursMap["bogashlugbovya12_5"] = R.raw.bogashlugbovya12_5
            resursMap["bogashlugbovya12_6"] = R.raw.bogashlugbovya12_6
            resursMap["bogashlugbovya12_7"] = R.raw.bogashlugbovya12_7
            resursMap["bogashlugbovya12_8"] = R.raw.bogashlugbovya12_8
            resursMap["bogashlugbovya12_9"] = R.raw.bogashlugbovya12_9
            resursMap["bogashlugbovya13_1"] = R.raw.bogashlugbovya13_1
            resursMap["bogashlugbovya13_2"] = R.raw.bogashlugbovya13_2
            resursMap["bogashlugbovya13_3"] = R.raw.bogashlugbovya13_3
            resursMap["bogashlugbovya13_4"] = R.raw.bogashlugbovya13_4
            resursMap["bogashlugbovya13_5"] = R.raw.bogashlugbovya13_5
            resursMap["bogashlugbovya13_6"] = R.raw.bogashlugbovya13_6
            resursMap["bogashlugbovya13_7"] = R.raw.bogashlugbovya13_7
            resursMap["bogashlugbovya13_8"] = R.raw.bogashlugbovya13_8
            resursMap["bogashlugbovya14_1"] = R.raw.bogashlugbovya14_1
            resursMap["bogashlugbovya14_2"] = R.raw.bogashlugbovya14_2
            resursMap["bogashlugbovya14_3"] = R.raw.bogashlugbovya14_3
            resursMap["bogashlugbovya14_4"] = R.raw.bogashlugbovya14_4
            resursMap["bogashlugbovya14_5"] = R.raw.bogashlugbovya14_5
            resursMap["bogashlugbovya14_6"] = R.raw.bogashlugbovya14_6
            resursMap["bogashlugbovya14_7"] = R.raw.bogashlugbovya14_7
            resursMap["bogashlugbovya14_8"] = R.raw.bogashlugbovya14_8
            resursMap["bogashlugbovya14_9"] = R.raw.bogashlugbovya14_9
            resursMap["bogashlugbovya15_1"] = R.raw.bogashlugbovya15_1
            resursMap["bogashlugbovya15_2"] = R.raw.bogashlugbovya15_2
            resursMap["bogashlugbovya15_3"] = R.raw.bogashlugbovya15_3
            resursMap["bogashlugbovya15_4"] = R.raw.bogashlugbovya15_4
            resursMap["bogashlugbovya15_5"] = R.raw.bogashlugbovya15_5
            resursMap["bogashlugbovya15_6"] = R.raw.bogashlugbovya15_6
            resursMap["bogashlugbovya15_7"] = R.raw.bogashlugbovya15_7
            resursMap["bogashlugbovya15_8"] = R.raw.bogashlugbovya15_8
            resursMap["bogashlugbovya15_9"] = R.raw.bogashlugbovya15_9
            resursMap["bogashlugbovya16_1"] = R.raw.bogashlugbovya16_1
            resursMap["bogashlugbovya16_2"] = R.raw.bogashlugbovya16_2
            resursMap["bogashlugbovya16_3"] = R.raw.bogashlugbovya16_3
            resursMap["bogashlugbovya16_4"] = R.raw.bogashlugbovya16_4
            resursMap["bogashlugbovya16_5"] = R.raw.bogashlugbovya16_5
            resursMap["bogashlugbovya16_6"] = R.raw.bogashlugbovya16_6
            resursMap["bogashlugbovya16_7"] = R.raw.bogashlugbovya16_7
            resursMap["bogashlugbovya16_8"] = R.raw.bogashlugbovya16_8
            resursMap["bogashlugbovya16_9"] = R.raw.bogashlugbovya16_9
            resursMap["bogashlugbovya16_10"] = R.raw.bogashlugbovya16_10
            resursMap["bogashlugbovya16_11"] = R.raw.bogashlugbovya16_11
            resursMap["bogashlugbovya17_1"] = R.raw.bogashlugbovya17_1
            resursMap["bogashlugbovya17_2"] = R.raw.bogashlugbovya17_2
            resursMap["bogashlugbovya17_3"] = R.raw.bogashlugbovya17_3
            resursMap["bogashlugbovya17_4"] = R.raw.bogashlugbovya17_4
            resursMap["bogashlugbovya17_5"] = R.raw.bogashlugbovya17_5
            resursMap["bogashlugbovya17_6"] = R.raw.bogashlugbovya17_6
            resursMap["bogashlugbovya17_7"] = R.raw.bogashlugbovya17_7
            resursMap["bogashlugbovya17_8"] = R.raw.bogashlugbovya17_8
            resursMap["akafist0"] = R.raw.akafist0
            resursMap["akafist1"] = R.raw.akafist1
            resursMap["akafist2"] = R.raw.akafist2
            resursMap["akafist3"] = R.raw.akafist3
            resursMap["akafist4"] = R.raw.akafist4
            resursMap["akafist5"] = R.raw.akafist5
            resursMap["akafist6"] = R.raw.akafist6
            resursMap["akafist7"] = R.raw.akafist7
            resursMap["malitvy1"] = R.raw.malitvy1
            resursMap["malitvy2"] = R.raw.malitvy2
            resursMap["paslia_prychascia1"] = R.raw.paslia_prychascia1
            resursMap["paslia_prychascia2"] = R.raw.paslia_prychascia2
            resursMap["paslia_prychascia3"] = R.raw.paslia_prychascia3
            resursMap["paslia_prychascia4"] = R.raw.paslia_prychascia4
            resursMap["paslia_prychascia5"] = R.raw.paslia_prychascia5
            resursMap["prynagodnyia_0"] = R.raw.prynagodnyia_0
            resursMap["prynagodnyia_1"] = R.raw.prynagodnyia_1
            resursMap["prynagodnyia_2"] = R.raw.prynagodnyia_2
            resursMap["prynagodnyia_3"] = R.raw.prynagodnyia_3
            resursMap["prynagodnyia_4"] = R.raw.prynagodnyia_4
            resursMap["prynagodnyia_5"] = R.raw.prynagodnyia_5
            resursMap["prynagodnyia_6"] = R.raw.prynagodnyia_6
            resursMap["prynagodnyia_7"] = R.raw.prynagodnyia_7
            resursMap["prynagodnyia_8"] = R.raw.prynagodnyia_8
            resursMap["prynagodnyia_9"] = R.raw.prynagodnyia_9
            resursMap["prynagodnyia_10"] = R.raw.prynagodnyia_10
            resursMap["prynagodnyia_11"] = R.raw.prynagodnyia_11
            resursMap["prynagodnyia_12"] = R.raw.prynagodnyia_12
            resursMap["prynagodnyia_13"] = R.raw.prynagodnyia_13
            resursMap["prynagodnyia_14"] = R.raw.prynagodnyia_14
            resursMap["prynagodnyia_15"] = R.raw.prynagodnyia_15
            resursMap["prynagodnyia_16"] = R.raw.prynagodnyia_16
            resursMap["prynagodnyia_17"] = R.raw.prynagodnyia_17
            resursMap["prynagodnyia_18"] = R.raw.prynagodnyia_18
            resursMap["prynagodnyia_19"] = R.raw.prynagodnyia_19
            resursMap["prynagodnyia_20"] = R.raw.prynagodnyia_20
            resursMap["prynagodnyia_21"] = R.raw.prynagodnyia_21
            resursMap["prynagodnyia_22"] = R.raw.prynagodnyia_22
            resursMap["prynagodnyia_23"] = R.raw.prynagodnyia_23
            resursMap["prynagodnyia_24"] = R.raw.prynagodnyia_24
            resursMap["prynagodnyia_25"] = R.raw.prynagodnyia_25
            resursMap["prynagodnyia_26"] = R.raw.prynagodnyia_26
            resursMap["prynagodnyia_27"] = R.raw.prynagodnyia_27
            resursMap["prynagodnyia_28"] = R.raw.prynagodnyia_28
            resursMap["prynagodnyia_29"] = R.raw.prynagodnyia_29
            resursMap["prynagodnyia_30"] = R.raw.prynagodnyia_30
            resursMap["prynagodnyia_31"] = R.raw.prynagodnyia_31
            resursMap["prynagodnyia_32"] = R.raw.prynagodnyia_32
            resursMap["prynagodnyia_33"] = R.raw.prynagodnyia_33
            resursMap["prynagodnyia_34"] = R.raw.prynagodnyia_34
            resursMap["prynagodnyia_35"] = R.raw.prynagodnyia_35
            resursMap["prynagodnyia_36"] = R.raw.prynagodnyia_36
            resursMap["ruzanec0"] = R.raw.ruzanec0
            resursMap["ruzanec1"] = R.raw.ruzanec1
            resursMap["ruzanec2"] = R.raw.ruzanec2
            resursMap["ruzanec3"] = R.raw.ruzanec3
            resursMap["ruzanec4"] = R.raw.ruzanec4
            resursMap["ruzanec5"] = R.raw.ruzanec5
            resursMap["ruzanec6"] = R.raw.ruzanec6
            resursMap["ton1"] = R.raw.ton1
            resursMap["ton1_budni"] = R.raw.ton1_budni
            resursMap["ton2"] = R.raw.ton2
            resursMap["ton2_budni"] = R.raw.ton2_budni
            resursMap["ton3"] = R.raw.ton3
            resursMap["ton3_budni"] = R.raw.ton3_budni
            resursMap["ton4"] = R.raw.ton4
            resursMap["ton4_budni"] = R.raw.ton4_budni
            resursMap["ton5"] = R.raw.ton5
            resursMap["ton5_budni"] = R.raw.ton5_budni
            resursMap["ton6"] = R.raw.ton6
            resursMap["ton6_budni"] = R.raw.ton6_budni
            resursMap["ton7"] = R.raw.ton7
            resursMap["ton8"] = R.raw.ton8
            PesnyAll.resursMap.forEach {
                resursMap[it.key] = it.value
            }
        }

        fun setVybranoe(context: Context, resurs: String, title: String): Boolean {
            var check = true
            val file = File(context.filesDir.toString() + "/Vybranoe.json")
            try {
                val gson = Gson()
                if (file.exists()) {
                    val type = object : TypeToken<ArrayList<VybranoeData>>() {}.type
                    MenuVybranoe.vybranoe = gson.fromJson(file.readText(), type)
                }
                for (i in 0 until MenuVybranoe.vybranoe.size) {
                    if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) {
                        MenuVybranoe.vybranoe.removeAt(i)
                        check = false
                        break
                    }
                }
                if (check) {
                    MenuVybranoe.vybranoe.add(0, VybranoeData(vybranoeIndex(), resurs, title))
                }
                file.writer().use {
                    it.write(gson.toJson(MenuVybranoe.vybranoe))
                }
            } catch (t: Throwable) {
                file.delete()
                check = false
            }
            return check
        }

        fun vybranoeIndex(): Long {
            var result: Long = 1
            val vybranoe = MenuVybranoe.vybranoe
            if (vybranoe.size != 0) {
                vybranoe.forEach {
                    if (result < it.id) result = it.id
                }
                result++
            }
            return result
        }

        fun checkVybranoe(context: Context, resurs: String): Boolean {
            val file = File(context.filesDir.toString() + "/Vybranoe.json")
            try {
                if (file.exists()) {
                    val gson = Gson()
                    val type = object : TypeToken<ArrayList<VybranoeData>>() {}.type
                    MenuVybranoe.vybranoe = gson.fromJson(file.readText(), type)
                } else {
                    return false
                }
                for (i in 0 until MenuVybranoe.vybranoe.size) {
                    if (MenuVybranoe.vybranoe[i].resurs.intern() == resurs) return true
                }
            } catch (t: Throwable) {
                file.delete()
                return false
            }
            return false
        }
    }

    override fun onDialogFontSize(fontSize: Float) {
        fontBiblia = fontSize
        if (binding.scrollView2.visibility == View.VISIBLE) {
            binding.TextView.textSize = fontBiblia
        } else {
            val webSettings = binding.WebView.settings
            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
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

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (k.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        binding = BogasluzbovyaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resurs = intent?.getStringExtra("resurs") ?: ""
        if (resurs.contains("pesny")) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        title = intent?.getStringExtra("title") ?: ""
        loadData(savedInstanceState)
        autoscroll = k.getBoolean("autoscroll", false)
        spid = k.getInt("autoscrollSpid", 60)
        binding.WebView.setOnTouchListener(this)
        val client = MyWebViewClient()
        client.setOnLinkListenner(this)
        binding.WebView.webViewClient = client
        binding.scrollView2.setOnScrollChangedCallback(this)
        binding.constraint.setOnTouchListener(this)
        autoscroll = k.getBoolean("autoscroll", false)
        if (savedInstanceState != null) {
            fullscreenPage = savedInstanceState.getBoolean("fullscreen")
            editVybranoe = savedInstanceState.getBoolean("editVybranoe")
            MainActivity.dialogVisable = false
            if (savedInstanceState.getBoolean("seach")) {
                binding.textSearch.visibility = View.VISIBLE
                binding.textCount.visibility = View.VISIBLE
                binding.imageView6.visibility = View.VISIBLE
                binding.imageView5.visibility = View.VISIBLE
            }
        }
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_DEFAULT_FONT_SIZE)
        binding.TextView.textSize = fontBiblia
        if (dzenNoch) {
            binding.progress.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
            binding.WebView.setBackgroundColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorbackground_material_dark))
        }
        men = checkVybranoe(this, resurs)
        val webSettings = binding.WebView.settings
        webSettings.standardFontFamily = "sans-serif-condensed"
        webSettings.defaultFontSize = fontBiblia.toInt()
        webSettings.javaScriptEnabled = true
        positionY = (k.getInt(resurs + "Scroll", 0) / resources.displayMetrics.density).toInt()
        binding.WebView.setOnScrollChangedCallback(this)
        binding.WebView.setOnBottomListener(this)
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
        binding.titleToolbar.setOnClickListener {
            val layoutParams = binding.toolbar.layoutParams
            if (binding.titleToolbar.isSelected) {
                resetTollbarJob?.cancel()
                resetTollbar(layoutParams)
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.titleToolbar.isSingleLine = false
                binding.titleToolbar.isSelected = true
                resetTollbarJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(5000)
                    resetTollbar(layoutParams)
                }
            }
        }
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = title
        if (dzenNoch) {
            binding.toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
        }
    }

    private fun resetTollbar(layoutParams: ViewGroup.LayoutParams) {
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            layoutParams.height = actionBarHeight
        }
        binding.titleToolbar.isSelected = false
        binding.titleToolbar.isSingleLine = true
    }

    private fun scrollWebView(): StringBuilder {
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

    private fun loadData(savedInstanceState: Bundle?) = CoroutineScope(Dispatchers.Main).launch {
        binding.progressBar.visibility = View.VISIBLE
        val res = withContext(Dispatchers.IO) {
            val builder = StringBuilder()
            val id = resursMap[resurs] ?: R.raw.bogashlugbovya1
            val inputStream: InputStream = resources.openRawResource(id)
            val zmenyiaChastki = ZmenyiaChastki(this@Bogashlugbovya)
            val gregorian = Calendar.getInstance() as GregorianCalendar
            val dayOfWeek = gregorian.get(Calendar.DAY_OF_WEEK)
            val isr = InputStreamReader(inputStream)
            val reader = BufferedReader(isr)
            val color = if (dzenNoch) "<font color=\"#f44336\">"
            else "<font color=\"#d00505\">"
            reader.forEachLine {
                var line = it
                if (dzenNoch) line = line.replace("#d00505", "#f44336")
                line = line.replace("<head>", "<head>" + scrollWebView())
                line = line.replace("<body>", "<body onload='toY()'>")
                line = if (dzenNoch) line.replace("<html><head>", "<html><head><style type=\"text/css\">::selection {background: #eb9b9a} body{-webkit-tap-highlight-color: rgba(244,67,54,0.2); color: #fff; background-color: #303030; margin: 0; padding: 0}</style>")
                else line.replace("<html><head>", "<html><head><style type=\"text/css\">::selection {background: #eb9b9a} body{-webkit-tap-highlight-color: rgba(208,5,5,0.1); margin: 0; padding: 0}</style>")
                if (resurs.contains("bogashlugbovya")) {
                    if (line.contains("<KANDAK></KANDAK>")) {
                        line = line.replace("<KANDAK></KANDAK>", "")
                        builder.append(line)
                        try {
                            if (dayOfWeek == 1) {
                                builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(1))
                            } else {
                                builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 1))
                            }
                        } catch (t: Throwable) {
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
                        }
                    }
                    if (line.contains("<PRAKIMEN></PRAKIMEN>")) {
                        line = line.replace("<PRAKIMEN></PRAKIMEN>", "")
                        builder.append(line)
                        try {
                            if (dayOfWeek == 1) {
                                builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(2))
                            } else {
                                builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 2))
                            }
                        } catch (t: Throwable) {
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
                        }
                    }
                    if (line.contains("<ALILUIA></ALILUIA>")) {
                        line = line.replace("<ALILUIA></ALILUIA>", "")
                        builder.append(line)
                        try {
                            if (dayOfWeek == 1) {
                                builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(3))
                            } else {
                                builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 3))
                            }
                        } catch (t: Throwable) {
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
                        }
                    }
                    if (line.contains("<PRICHASNIK></PRICHASNIK>")) {
                        line = line.replace("<PRICHASNIK></PRICHASNIK>", "")
                        builder.append(line)
                        try {
                            if (dayOfWeek == 1) {
                                builder.append(zmenyiaChastki.traparyIKandakiNiadzelnyia(4))
                            } else {
                                builder.append(zmenyiaChastki.traparyIKandakiNaKognyDzen(dayOfWeek, 4))
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
                        }
                    }
                    when {
                        line.contains("<APCH></APCH>") -> {
                            line = line.replace("<APCH></APCH>", "")
                            var sv = zmenyiaChastki.sviatyia()
                            if (sv != "") {
                                val s1 = sv.split(":")
                                val s2 = s1[1].split(";")
                                sv = s1[0] + ":" + s2[0]
                                builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=8\">").append(color).append(sv).append("</font></a>").append("<br><br>\n")
                            } else builder.append(line)
                            var svDop = zmenyiaChastki.sviatyiaDop()
                            if (svDop != "") {
                                val s1 = svDop.split(":")
                                val s2 = s1[1].split(";")
                                svDop = s1[0] + ":" + s2[0]
                                builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=8\">").append(color).append(svDop).append("</font></a>").append("<br><br>\n")
                            } else builder.append(line)
                            try {
                                builder.append(zmenyiaChastki.zmenya(1))
                            } catch (t: Throwable) {
                                builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
                            }
                        }
                        line.contains("<EVCH></EVCH>") -> {
                            line = line.replace("<EVCH></EVCH>", "")
                            var sv = zmenyiaChastki.sviatyia()
                            if (sv != "") {
                                val s1 = sv.split(":")
                                val s2 = s1[1].split(";")
                                sv = s1[0] + ":" + s2[1]
                                builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=9\">").append(color).append(sv).append("</font></a>").append("<br><br>\n")
                            } else builder.append(line)
                            var svDop = zmenyiaChastki.sviatyiaDop()
                            if (svDop != "") {
                                val s1 = svDop.split(":")
                                val s2 = s1[1].split(";")
                                svDop = s1[0] + ":" + s2[1]
                                builder.append("<a href=\"https://m.carkva-gazeta.by/index.php?Alert=9\">").append(color).append(svDop).append("</font></a>").append("<br><br>\n")
                            } else builder.append(line)
                            try {
                                builder.append(zmenyiaChastki.zmenya(0))
                            } catch (t: Throwable) {
                                builder.append(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch)).append("<br>\n")
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
            return@withContext builder.toString()
        }
        var bogashlugbovya = true
        if (resurs.contains("bogashlugbovya")) {
            val t1 = resurs.indexOf("_")
            if (t1 != -1) bogashlugbovya = false
        }
        if ((resurs.contains("bogashlugbovya") && bogashlugbovya) || resurs.contains("akafist") || resurs.contains("malitvy") || resurs.contains("ruzanec") || resurs.contains("ton")) {
            binding.scrollView2.visibility = View.GONE
            if (resurs.contains("ton")) mAutoScroll = false
            binding.WebView.visibility = View.VISIBLE
            binding.WebView.loadDataWithBaseURL("malitounikApp-app//carkva-gazeta.by/", res, "text/html", "utf-8", null)
            if (savedInstanceState == null) {
                if (k.getBoolean("autoscrollAutostart", false) && mAutoScroll) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    autoStartScroll()
                }
            }
        } else {
            binding.WebView.visibility = View.GONE
            binding.scrollView2.visibility = View.VISIBLE
            binding.TextView.text = MainActivity.fromHtml(res)
            positionY = k.getInt(resurs + "Scroll", 0)
            binding.scrollView2.post { binding.scrollView2.scrollBy(0, positionY) }
            mAutoScroll = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            binding.WebView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                if (numberOfMatches == 0) binding.textCount.setText(by.carkva_gazeta.malitounik.R.string.niama)
                else binding.textCount.text = (activeMatchOrdinal + 1).toString().plus(" ($numberOfMatches)")
            }
            if (dzenNoch) binding.imageView6.setImageResource(by.carkva_gazeta.malitounik.R.drawable.up_black)
            binding.imageView6.setOnClickListener { binding.WebView.findNext(false) }
            binding.textSearch.addTextChangedListener(object : TextWatcher {
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
                            binding.textSearch.removeTextChangedListener(this)
                            binding.textSearch.setText(edit)
                            binding.textSearch.setSelection(editPosition)
                            binding.textSearch.addTextChangedListener(this)
                        }
                    }
                    binding.WebView.findAllAsync(edit)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    editch = count != after
                    check = after
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    editPosition = start + count
                }
            })
            if (dzenNoch) binding.imageView5.setImageResource(by.carkva_gazeta.malitounik.R.drawable.niz_back)
            binding.imageView5.setOnClickListener { binding.WebView.findNext(true) }
        }
        invalidateOptionsMenu()
        binding.progressBar.visibility = View.GONE
    }

    private fun autoStartScroll() {
        if (autoScrollJob?.isActive != true) {
            var autoTime: Long = 10000
            for (i in 0..15) {
                if (i == k.getInt("autoscrollAutostartTime", 5)) {
                    autoTime = (i + 5) * 1000L
                    break
                }
            }
            autoStartScrollJob = CoroutineScope(Dispatchers.Main).launch {
                delay(autoTime)
                startAutoScroll()
                val prefEditor: Editor = k.edit()
                prefEditor.putBoolean("autoscroll", true)
                prefEditor.apply()
                invalidateOptionsMenu()
            }
        }
    }

    private fun stopAutoStartScroll() {
        autoStartScrollJob?.cancel()
    }

    private fun stopProcent() {
        procentJob?.cancel()
    }

    private fun startProcent() {
        stopProcent()
        procentJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            binding.progress.visibility = View.GONE
        }
    }

    private fun stopAutoScroll(delayDisplayOff: Boolean = true) {
        autoScrollJob?.cancel()
        if (!k.getBoolean("scrinOn", false) && delayDisplayOff) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(60000)
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun startAutoScroll() {
        stopAutoStartScroll()
        autoScrollJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(spid.toLong())
                if (!mActionDown && !MainActivity.dialogVisable) {
                    binding.WebView.scrollBy(0, 2)
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val heightConstraintLayout = binding.constraint.height
        val widthConstraintLayout = binding.constraint.width
        val otstup = (10 * resources.displayMetrics.density).toInt()
        val y = event?.y?.toInt() ?: 0
        val x = event?.x?.toInt() ?: 0
        val prefEditor: Editor = k.edit()
        val id = v?.id ?: 0
        if (id == R.id.WebView) {
            stopAutoStartScroll()
            when (event?.action ?: MotionEvent.ACTION_CANCEL) {
                MotionEvent.ACTION_DOWN -> mActionDown = true
                MotionEvent.ACTION_UP -> mActionDown = false
                MotionEvent.ACTION_MOVE -> {
                    val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.textSearch.windowToken, 0)
                }
            }
            return false
        }
        if (id == R.id.constraint) {
            if (MainActivity.checkBrightness) {
                MainActivity.brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) * 100 / 255
            }
            when (event?.action ?: MotionEvent.ACTION_CANCEL) {
                MotionEvent.ACTION_DOWN -> {
                    n = event?.y?.toInt() ?: 0
                    yS = event?.x?.toInt() ?: 0
                    val proc: Int
                    if (x < otstup) {
                        levo = true
                        binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                        binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                        binding.progress.visibility = View.VISIBLE
                        startProcent()
                    }
                    if (x > widthConstraintLayout - otstup) {
                        pravo = true
                        var minmax = ""
                        if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MIN) minmax = " (мін)"
                        if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MAX) minmax = " (макс)"
                        binding.progress.text = getString(by.carkva_gazeta.malitounik.R.string.font_sp, fontBiblia.toInt(), minmax)
                        binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                        binding.progress.visibility = View.VISIBLE
                        startProcent()
                    }
                    if (y > heightConstraintLayout - otstup) {
                        niz = true
                        spid = k.getInt("autoscrollSpid", 60)
                        proc = 100 - (spid - 15) * 100 / 215
                        binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                        binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                        binding.progress.visibility = View.VISIBLE
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
                            binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                            MainActivity.checkBrightness = false
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                    if (x < otstup && y < n && y % 15 == 0) {
                        if (MainActivity.brightness < 100) {
                            MainActivity.brightness = MainActivity.brightness + 1
                            val lp = window.attributes
                            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
                            window.attributes = lp
                            binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                            MainActivity.checkBrightness = false
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                    if (x > widthConstraintLayout - otstup && y > n && y % 26 == 0) {
                        if (fontBiblia > SettingsActivity.GET_FONT_SIZE_MIN) {
                            fontBiblia -= 4
                            prefEditor.putFloat("font_biblia", fontBiblia)
                            prefEditor.apply()
                            val webSettings = binding.WebView.settings
                            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
                            //webSettings.setAppCacheEnabled(false)
                            webSettings.blockNetworkImage = true
                            webSettings.loadsImagesAutomatically = true
                            webSettings.setGeolocationEnabled(false)
                            webSettings.setNeedInitialFocus(false)
                            webSettings.defaultFontSize = fontBiblia.toInt()
                            var min = ""
                            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MIN) min = " (мін)"
                            binding.progress.text = getString(by.carkva_gazeta.malitounik.R.string.font_sp, fontBiblia.toInt(), min)
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                    if (x > widthConstraintLayout - otstup && y < n && y % 26 == 0) {
                        if (fontBiblia < SettingsActivity.GET_FONT_SIZE_MAX) {
                            fontBiblia += 4
                            prefEditor.putFloat("font_biblia", fontBiblia)
                            prefEditor.apply()
                            val webSettings = binding.WebView.settings
                            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
                            //webSettings.setAppCacheEnabled(false)
                            webSettings.blockNetworkImage = true
                            webSettings.loadsImagesAutomatically = true
                            webSettings.setGeolocationEnabled(false)
                            webSettings.setNeedInitialFocus(false)
                            webSettings.defaultFontSize = fontBiblia.toInt()
                            var max = ""
                            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MAX) max = " (макс)"
                            binding.progress.text = getString(by.carkva_gazeta.malitounik.R.string.font_sp, fontBiblia.toInt(), max)
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                    if (y > heightConstraintLayout - otstup && x > yS && x % 25 == 0) {
                        if (spid in 20..235) {
                            spid -= 5
                            val proc = 100 - (spid - 15) * 100 / 215
                            binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                            binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                    if (y > heightConstraintLayout - otstup && x < yS && x % 25 == 0) {
                        if (spid in 10..225) {
                            spid += 5
                            val proc = 100 - (spid - 15) * 100 / 215
                            binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                            binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                            binding.progress.visibility = View.VISIBLE
                            startProcent()
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v?.performClick()
                    if (levo) {
                        levo = false
                    }
                    if (pravo) {
                        pravo = false
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
                    }
                    if (pravo) {
                        pravo = false
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
        val itemAuto = menu.findItem(by.carkva_gazeta.malitounik.R.id.action_auto)
        val itemVybranoe: MenuItem = menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe)
        if (resurs.contains("bogashlugbovya") || resurs.contains("akafist") || resurs.contains("malitvy") || resurs.contains("ruzanec")) {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_share).isVisible = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) menu.findItem(by.carkva_gazeta.malitounik.R.id.action_find).isVisible = true
        }
        if (mAutoScroll) {
            autoscroll = k.getBoolean("autoscroll", false)
            if (autoscroll) {
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_plus).isVisible = true
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_minus).isVisible = true
                itemAuto.title = resources.getString(by.carkva_gazeta.malitounik.R.string.autoScrolloff)
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            } else {
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_plus).isVisible = false
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_minus).isVisible = false
                itemAuto.title = resources.getString(by.carkva_gazeta.malitounik.R.string.autoScrollon)
                menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
        } else {
            itemAuto.isVisible = false
            stopAutoScroll()
        }
        var spanString = SpannableString(itemAuto.title.toString())
        var end = spanString.length
        spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        itemAuto.title = spanString

        if (men) {
            itemVybranoe.icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_on)
            itemVybranoe.title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe_del)
        } else {
            itemVybranoe.icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_off)
            itemVybranoe.title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe)
        }
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_orientation).isChecked = k.getBoolean("orientation", false)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_dzen_noch).isChecked = k.getBoolean("dzen_noch", false)

        spanString = SpannableString(itemVybranoe.title.toString())
        end = spanString.length
        spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        itemVybranoe.title = spanString
        return true
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        stopAutoStartScroll()
        if (featureId == AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR && autoscroll) {
            MainActivity.dialogVisable = true
        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        if (featureId == AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR && autoscroll) {
            MainActivity.dialogVisable = false
        }
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
        if (id == by.carkva_gazeta.malitounik.R.id.action_help) {
            startActivity(Intent(this, HelpText::class.java))
        }
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
            binding.textSearch.visibility = View.VISIBLE
            binding.textCount.visibility = View.VISIBLE
            binding.imageView6.visibility = View.VISIBLE
            binding.imageView5.visibility = View.VISIBLE
            binding.textSearch.requestFocus()
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
                binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                binding.progress.visibility = View.VISIBLE
                startProcent()
                val prefEditors = k.edit()
                prefEditors.putInt("autoscrollSpid", spid)
                prefEditors.apply()
            }
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_minus) {
            if (spid in 10..225) {
                spid += 5
                val proc = 100 - (spid - 15) * 100 / 215
                binding.progress.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50f)
                binding.progress.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                binding.progress.visibility = View.VISIBLE
                startProcent()
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
                MainActivity.toastView(this, getString(by.carkva_gazeta.malitounik.R.string.addVybranoe))
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
        if (id == by.carkva_gazeta.malitounik.R.id.action_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://carkva-gazeta.by/share/index.php?pub=2&file=$resurs")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, null))
        }
        prefEditor.apply()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else if (binding.textSearch.visibility == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                binding.textSearch.visibility = View.GONE
                binding.textCount.visibility = View.GONE
                binding.imageView6.visibility = View.GONE
                binding.imageView5.visibility = View.GONE
                binding.WebView.findAllAsync("")
                binding.textSearch.setText("")
                binding.textCount.setText(by.carkva_gazeta.malitounik.R.string.niama)
                val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.textSearch.windowToken, 0)
            }
        } else {
            if (editVybranoe) onSupportNavigateUp()
            else super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        val prefEditor = k.edit()
        prefEditor.putInt(resurs + "Scroll", positionY)
        prefEditor.apply()
        stopAutoScroll(false)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        autoStartScrollJob?.cancel()
        procentJob?.cancel()
        resetTollbarJob?.cancel()
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

    @Suppress("DEPRECATION")
    private fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, ulAnimationDelay.toLong())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("fullscreen", fullscreenPage)
        outState.putBoolean("editVybranoe", editVybranoe)
        if (binding.textSearch.visibility == View.VISIBLE) outState.putBoolean("seach", true)
        else outState.putBoolean("seach", false)
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
