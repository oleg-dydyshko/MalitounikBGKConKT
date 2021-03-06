package by.carkva_gazeta.resources

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.malitounik.DialogFontSize.DialogFontSizeListener
import by.carkva_gazeta.malitounik.InteractiveScrollView.OnBottomReachedListener
import by.carkva_gazeta.resources.databinding.AkafistChytanneBinding
import by.carkva_gazeta.resources.databinding.ProgressBinding
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

class Chytanne : AppCompatActivity(), OnTouchListener, DialogFontSizeListener, InteractiveScrollView.OnScrollChangedCallback {

    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private fun mHidePart2Runnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    private fun mShowPart2Runnable() {
        val actionBar = supportActionBar
        actionBar?.show()
    }

    private var fullscreenPage = false
    private lateinit var k: SharedPreferences
    private var fontBiblia = SettingsActivity.GET_FONT_SIZE_DEFAULT
    private var dzenNoch = false
    private var autoscroll = false
    private var n = 0
    private var spid = 60
    private var mActionDown = false
    private var change = false
    private lateinit var binding: AkafistChytanneBinding
    private lateinit var bindingprogress: ProgressBinding
    private var autoScrollJob: Job? = null
    private var autoStartScrollJob: Job? = null
    private var procentJob: Job? = null
    private var resetTollbarJob: Job? = null
    private var diffScroll = -1
    private var titleTwo = ""
    private var firstTextPosition = ""
    private var onRestore = false

    override fun onDialogFontSize(fontSize: Float) {
        fontBiblia = fontSize
        binding.textView.textSize = fontBiblia
    }

    @SuppressLint("ClickableViewAccessibility")
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
        binding = AkafistChytanneBinding.inflate(layoutInflater)
        bindingprogress = binding.progressView
        setContentView(binding.root)
        if (savedInstanceState != null) {
            MainActivity.dialogVisable = false
            fullscreenPage = savedInstanceState.getBoolean("fullscreen")
            change = savedInstanceState.getBoolean("change")
        } else {
            if (k.getBoolean("autoscrollAutostart", false)) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                autoStartScroll()
            }
        }
        fontBiblia = k.getFloat("font_biblia", SettingsActivity.GET_FONT_SIZE_DEFAULT)
        binding.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontBiblia)
        binding.constraint.setOnTouchListener(this)
        binding.InteractiveScroll.setOnBottomReachedListener(object : OnBottomReachedListener {
            override fun onBottomReached() {
                autoscroll = false
                stopAutoScroll()
                invalidateOptionsMenu()
            }

            override fun onScrollDiff(diff: Int) {
                diffScroll = diff
            }

            override fun onTouch(action: Boolean) {
                stopAutoStartScroll()
                mActionDown = action
            }
        })
        if (dzenNoch) {
            bindingprogress.progressText.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
            bindingprogress.progressTitle.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
            binding.actionPlus.background = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.selector_dark_maranata_buttom)
            binding.actionMinus.background = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.selector_dark_maranata_buttom)
        }
        spid = k.getInt("autoscrollSpid", 60)
        autoscroll = k.getBoolean("autoscroll", false)
        setChtenia(savedInstanceState)
        bindingprogress.fontSizePlus.setOnClickListener {
            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MAX)  bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.max_font)
            if (fontBiblia < SettingsActivity.GET_FONT_SIZE_MAX) {
                fontBiblia += 4
                bindingprogress.progressText.text = getString(by.carkva_gazeta.malitounik.R.string.get_font, fontBiblia.toInt())
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.font_size)
                bindingprogress.progress.visibility = View.VISIBLE
                val prefEditor: Editor = k.edit()
                prefEditor.putFloat("font_biblia", fontBiblia)
                prefEditor.apply()
                onDialogFontSize(fontBiblia)
            }
            startProcent(3000)
        }
        bindingprogress.fontSizeMinus.setOnClickListener {
            if (fontBiblia == SettingsActivity.GET_FONT_SIZE_MIN)  bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.min_font)
            if (fontBiblia > SettingsActivity.GET_FONT_SIZE_MIN) {
                fontBiblia -= 4
                bindingprogress.progressText.text = getString(by.carkva_gazeta.malitounik.R.string.get_font, fontBiblia.toInt())
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.font_size)
                bindingprogress.progress.visibility = View.VISIBLE
                val prefEditor: Editor = k.edit()
                prefEditor.putFloat("font_biblia", fontBiblia)
                prefEditor.apply()
                onDialogFontSize(fontBiblia)
            }
            startProcent(3000)
        }
        bindingprogress.brighessPlus.setOnClickListener {
            if (MainActivity.brightness < 100) {
                MainActivity.brightness = MainActivity.brightness + 1
                val lp = window.attributes
                lp.screenBrightness = MainActivity.brightness.toFloat() / 100
                window.attributes = lp
                bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.Bright)
                bindingprogress.progress.visibility = View.VISIBLE
                MainActivity.checkBrightness = false
            }
            startProcent(3000)
        }
        bindingprogress.brighessMinus.setOnClickListener {
            if (MainActivity.brightness > 0) {
                MainActivity.brightness = MainActivity.brightness - 1
                val lp = window.attributes
                lp.screenBrightness = MainActivity.brightness.toFloat() / 100
                window.attributes = lp
                bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.Bright)
                bindingprogress.progress.visibility = View.VISIBLE
                MainActivity.checkBrightness = false
            }
            startProcent(3000)
        }
        binding.actionPlus.setOnClickListener {
            if (spid in 20..235) {
                spid -= 5
                val proc = 100 - (spid - 15) * 100 / 215
                bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.speed_auto_scroll)
                bindingprogress.progress.visibility = View.VISIBLE
                startProcent()
                val prefEditors = k.edit()
                prefEditors.putInt("autoscrollSpid", spid)
                prefEditors.apply()
            }
        }
        binding.actionMinus.setOnClickListener {
            if (spid in 10..225) {
                spid += 5
                val proc = 100 - (spid - 15) * 100 / 215
                bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.speed_auto_scroll)
                bindingprogress.progress.visibility = View.VISIBLE
                startProcent()
                val prefEditors = k.edit()
                prefEditors.putInt("autoscrollSpid", spid)
                prefEditors.apply()
            }
        }
        binding.InteractiveScroll.setOnScrollChangedCallback(this)
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
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.czytanne)
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

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.performClick()
        val heightConstraintLayout = binding.constraint.height
        val widthConstraintLayout = binding.constraint.width
        val otstup = (10 * resources.displayMetrics.density).toInt()
        val y = event?.y?.toInt() ?: 0
        val x = event?.x?.toInt() ?: 0
        val id = v?.id ?: 0
        if (id == R.id.constraint) {
            if (MainActivity.checkBrightness) {
                MainActivity.brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) * 100 / 255
            }
            when (event?.action ?: MotionEvent.ACTION_CANCEL) {
                MotionEvent.ACTION_DOWN -> {
                    n = event?.y?.toInt() ?: 0
                    val proc: Int
                    if (x < otstup) {
                        bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, MainActivity.brightness)
                        bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.Bright)
                        bindingprogress.progress.visibility = View.VISIBLE
                        bindingprogress.brighess.visibility = View.VISIBLE
                        startProcent(3000)
                    }
                    if (x > widthConstraintLayout - otstup) {
                        bindingprogress.progressText.text = getString(by.carkva_gazeta.malitounik.R.string.get_font, fontBiblia.toInt())
                        bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.font_size)
                        bindingprogress.progress.visibility = View.VISIBLE
                        bindingprogress.fontSize.visibility = View.VISIBLE
                        startProcent(3000)
                    }
                    if (y > heightConstraintLayout - otstup) {
                        spid = k.getInt("autoscrollSpid", 60)
                        proc = 100 - (spid - 15) * 100 / 215
                        bindingprogress.progressText.text = resources.getString(by.carkva_gazeta.malitounik.R.string.procent, proc)
                        bindingprogress.progressTitle.text = getString(by.carkva_gazeta.malitounik.R.string.speed_auto_scroll)
                        bindingprogress.progress.visibility = View.VISIBLE
                        startProcent()
                        autoscroll = k.getBoolean("autoscroll", false)
                        if (!autoscroll) {
                            startAutoScroll()
                            invalidateOptionsMenu()
                        }
                    }
                }
            }
        }
        return true
    }

    private fun setChtenia(savedInstanceState: Bundle?) {
        try {
            var w = intent.extras?.getString("cytanne") ?: ""
            val wOld = w
            w = MainActivity.removeZnakiAndSlovy(w)
            val split = w.split(";")
            var knigaN: String
            var knigaK = "0"
            var zaglnum = 0
            val ssbTitle = SpannableStringBuilder()
            var title = ""
            for (i in split.indices) {
                val zaglavie = split[i].split(",")
                var zagl = ""
                var zaglavieName = ""
                for (e in zaglavie.indices) {
                    try {
                        val zaglav = zaglavie[e].trim()
                        val zag = zaglav.indexOf(" ", 2)
                        val zag1 = zaglav.indexOf(".")
                        val zag2 = zaglav.indexOf("-")
                        val zag3 = zaglav.indexOf(".", zag1 + 1)
                        val zagS = if (zag2 != -1) {
                            zaglav.substring(0, zag2)
                        } else {
                            zaglav
                        }
                        var glav = false
                        if (zag1 > zag2 && zag == -1) {
                            glav = true
                        } else if (zag != -1) {
                            zagl = zaglav.substring(0, zag)
                            val zaglavieName1 = split[i].trim()
                            zaglavieName = " " + zaglavieName1.substring(zag + 1)
                            zaglnum = zaglav.substring(zag + 1, zag1).toInt()
                        } else if (zag1 != -1) {
                            zaglnum = zaglav.substring(0, zag1).toInt()
                        }
                        if (glav) {
                            val zagS1 = zagS.indexOf(".")
                            if (zagS1 == -1) {
                                knigaN = zagS // Начало чтения
                            } else {
                                zaglnum = zagS.substring(0, zagS1).toInt()
                                knigaN = zagS.substring(zagS1 + 1)
                            }
                        } else if (zag2 == -1) { // Конец чтения
                            if (zag != -1) {
                                val zagS1 = zagS.indexOf(".")
                                zaglnum = zagS.substring(zag + 1, zagS1).toInt()
                                knigaN = zagS.substring(zagS1 + 1)
                            } else {
                                knigaN = zaglav
                            }
                            knigaK = knigaN
                        } else {
                            knigaN = zaglav.substring(zag1 + 1, zag2)
                        }
                        if (glav) {
                            knigaK = zaglav.substring(zag1 + 1)
                        } else if (zag2 != -1) {
                            knigaK = if (zag3 == -1) {
                                zaglav.substring(zag2 + 1)
                            } else {
                                zaglav.substring(zag3 + 1)
                            }
                        }
                        var polstixaA = false
                        var polstixaB = false
                        if (knigaK.contains("а", true)) {
                            polstixaA = true
                            knigaK = knigaK.replace("а", "", true)
                        }
                        if (knigaN.contains("б", true)) {
                            polstixaB = true
                            knigaN = knigaN.replace("б", "", true)
                        }
                        var spln = ""
                        if (i > 0) spln = "\n"
                        var kniga = -1
                        if (zagl == "Мц") kniga = 0
                        if (zagl == "Мк") kniga = 1
                        if (zagl == "Лк") kniga = 2
                        if (zagl == "Ян") kniga = 3
                        if (zagl == "Дз") kniga = 4
                        if (zagl == "Як") kniga = 5
                        if (zagl == "1 Пт") kniga = 6
                        if (zagl == "2 Пт") kniga = 7
                        if (zagl == "1 Ян") kniga = 8
                        if (zagl == "2 Ян") kniga = 9
                        if (zagl == "3 Ян") kniga = 10
                        if (zagl == "Юд") kniga = 11
                        if (zagl == "Рым") kniga = 12
                        if (zagl == "1 Кар") kniga = 13
                        if (zagl == "2 Кар") kniga = 14
                        if (zagl == "Гал") kniga = 15
                        if (zagl == "Эф") kniga = 16
                        if (zagl == "Плп") kniga = 17
                        if (zagl == "Клс") kniga = 18
                        if (zagl == "1 Фес") kniga = 19
                        if (zagl == "2 Фес") kniga = 20
                        if (zagl == "1 Цім") kniga = 21
                        if (zagl == "2 Цім") kniga = 22
                        if (zagl == "Ціт") kniga = 23
                        if (zagl == "Піл") kniga = 24
                        if (zagl == "Гбр") kniga = 25
                        if (zagl == "Быц") kniga = 26
                        if (zagl == "Высл") kniga = 27
                        if (zagl == "Езк") kniga = 28
                        if (zagl == "Вых") kniga = 29
                        if (zagl == "Ёў") kniga = 30
                        if (zagl == "Зах") kniga = 31
                        if (zagl == "Ёіл") kniga = 32
                        if (zagl == "Саф") kniga = 33
                        if (zagl == "Іс") kniga = 34
                        if (zagl == "Ер") kniga = 35
                        if (zagl == "Дан") kniga = 36
                        if (zagl == "Лікі") kniga = 37
                        if (zagl == "Міх") kniga = 38
                        val r = resources
                        var inputStream = r.openRawResource(R.raw.biblian1)
                        var errorChytanne = false
                        when (kniga) {
                            0 -> {
                                inputStream = r.openRawResource(R.raw.biblian1)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_0, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            1 -> {
                                inputStream = r.openRawResource(R.raw.biblian2)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_1, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            2 -> {
                                inputStream = r.openRawResource(R.raw.biblian3)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_2, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            3 -> {
                                inputStream = r.openRawResource(R.raw.biblian4)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_3, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            4 -> {
                                inputStream = r.openRawResource(R.raw.biblian5)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_4, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            5 -> {
                                inputStream = r.openRawResource(R.raw.biblian6)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_5, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            6 -> {
                                inputStream = r.openRawResource(R.raw.biblian7)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_6, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            7 -> {
                                inputStream = r.openRawResource(R.raw.biblian8)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_7, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            8 -> {
                                inputStream = r.openRawResource(R.raw.biblian9)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_8, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            9 -> {
                                inputStream = r.openRawResource(R.raw.biblian10)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_9, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            10 -> {
                                inputStream = r.openRawResource(R.raw.biblian11)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_10, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            11 -> {
                                inputStream = r.openRawResource(R.raw.biblian12)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_11, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            12 -> {
                                inputStream = r.openRawResource(R.raw.biblian13)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_12, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            13 -> {
                                inputStream = r.openRawResource(R.raw.biblian14)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_13, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            14 -> {
                                inputStream = r.openRawResource(R.raw.biblian15)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_14, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            15 -> {
                                inputStream = r.openRawResource(R.raw.biblian16)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_15, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            16 -> {
                                inputStream = r.openRawResource(R.raw.biblian17)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_16, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            17 -> {
                                inputStream = r.openRawResource(R.raw.biblian18)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_17, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            18 -> {
                                inputStream = r.openRawResource(R.raw.biblian19)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_18, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            19 -> {
                                inputStream = r.openRawResource(R.raw.biblian20)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_19, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            20 -> {
                                inputStream = r.openRawResource(R.raw.biblian21)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_20, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            21 -> {
                                inputStream = r.openRawResource(R.raw.biblian22)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_21, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            22 -> {
                                inputStream = r.openRawResource(R.raw.biblian23)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_22, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            23 -> {
                                inputStream = r.openRawResource(R.raw.biblian24)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_23, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            24 -> {
                                inputStream = r.openRawResource(R.raw.biblian25)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_24, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            25 -> {
                                inputStream = r.openRawResource(R.raw.biblian26)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_25, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            26 -> {
                                inputStream = r.openRawResource(R.raw.biblias1)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_26, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            27 -> {
                                inputStream = r.openRawResource(R.raw.biblias20)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_27, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            28 -> {
                                inputStream = r.openRawResource(R.raw.biblias26)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_28, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            29 -> {
                                inputStream = r.openRawResource(R.raw.biblias2)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_29, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            30 -> {
                                inputStream = r.openRawResource(R.raw.biblias18)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_30, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            31 -> {
                                inputStream = r.openRawResource(R.raw.biblias38)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_31, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            32 -> {
                                inputStream = r.openRawResource(R.raw.biblias29)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_32, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            33 -> {
                                inputStream = r.openRawResource(R.raw.biblias36)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_33, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            34 -> {
                                inputStream = r.openRawResource(R.raw.biblias23)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_34, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            35 -> {
                                inputStream = r.openRawResource(R.raw.biblias24)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_35, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            36 -> {
                                inputStream = r.openRawResource(R.raw.biblias27)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_36, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            37 -> {
                                inputStream = r.openRawResource(R.raw.biblias4)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_37, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            38 -> {
                                inputStream = r.openRawResource(R.raw.biblias33)
                                title = if (e == 0) {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_38, spln, zaglavieName)
                                } else {
                                    resources.getString(by.carkva_gazeta.malitounik.R.string.chtinia_zag, spln.trim())
                                }
                            }
                            else -> {
                                errorChytanne = true
                            }
                        }
                        ssbTitle.append(title)
                        if (!errorChytanne) {
                            if (e == 0) ssbTitle.setSpan(StyleSpan(Typeface.BOLD), ssbTitle.length - title.length, ssbTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            val builder = StringBuilder()
                            InputStreamReader(inputStream).use { inputStreamReader ->
                                val reader = BufferedReader(inputStreamReader)
                                var line: String
                                reader.forEachLine {
                                    line = it
                                    if (line.contains("//")) {
                                        val t1 = line.indexOf("//")
                                        line = line.substring(0, t1).trim()
                                        if (line != "") builder.append(line).append("\n")
                                    } else {
                                        builder.append(line).append("\n")
                                    }
                                }
                            }
                            val split2 = builder.toString().split("===")
                            var spl: String
                            var desK1: Int
                            var desN: Int
                            spl = split2[zaglnum].trim()
                            desN = spl.indexOf("$knigaN.")
                            if (knigaN == knigaK && zag3 == -1) {
                                desK1 = desN
                            } else {
                                desK1 = spl.indexOf("$knigaK.")
                                if (desK1 == -1) {
                                    val splAll = spl.split("\n").size
                                    desK1 = spl.indexOf("$splAll.")
                                }
                                if (zag3 != -1 || glav) {
                                    val spl1 = split2[zaglnum].trim()
                                    val spl2 = split2[zaglnum + 1].trim()
                                    val des1 = spl1.length
                                    desN = spl1.indexOf("$knigaN.")
                                    desK1 = spl2.indexOf("$knigaK.")
                                    var desN1: Int = spl2.indexOf((knigaK.toInt() + 1).toString().plus("."), desK1)
                                    if (desN1 == -1) {
                                        desN1 = spl1.length
                                    }
                                    desK1 = desN1 + des1
                                    spl = spl1 + "\n" + spl2
                                    zaglnum += 1
                                }
                            }
                            var desK = spl.indexOf("\n", desK1)
                            if (desK == -1) {
                                desK = spl.length
                            }
                            val textBiblia = spl.substring(desN, desK).toSpannable()
                            if (polstixaA) {
                                val t2 = textBiblia.indexOf("$knigaK.")
                                val t3 = textBiblia.indexOf(".", t2)
                                var t1 = textBiblia.indexOf(":", t2)
                                if (t1 == -1) t1 = textBiblia.indexOf(";", t3 + 1)
                                if (t1 == -1) t1 = textBiblia.indexOf(".", t3 + 1)
                                if (t1 != -1) textBiblia.setSpan(StrikethroughSpan(), t1 + 1, textBiblia.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (polstixaB) {
                                val t2 = textBiblia.indexOf("\n")
                                val textPol = textBiblia.substring(0, t2 + 1)
                                val t3 = textPol.indexOf(".")
                                var t1 = textPol.indexOf(":")
                                if (t1 == -1) t1 = textPol.indexOf(";", t3 + 1)
                                if (t1 == -1) t1 = textPol.indexOf(".", t3 + 1)
                                if (t1 != -1) textBiblia.setSpan(StrikethroughSpan(), t3 + 1, t1 + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            ssbTitle.append("\n").append(textBiblia).append("\n")
                        } else {
                            error()
                        }
                    } catch (t: Throwable) {
                        error()
                    }
                    if (i == 1 && e == 0) titleTwo = title
                }
                binding.textView.text = ssbTitle.trim()
            }
            if (k.getBoolean("utran", true) && wOld.contains("На ютрані:") && savedInstanceState == null) {
                binding.textView.post {
                    val strPosition = binding.textView.text.indexOf(titleTwo.trim(), ignoreCase = true)
                    val line = binding.textView.layout.getLineForOffset(strPosition)
                    val y = binding.textView.layout.getLineTop(line)
                    val anim = ObjectAnimator.ofInt(binding.InteractiveScroll, "scrollY", binding.InteractiveScroll.scrollY, y)
                    anim.setDuration(1000).start()
                }
            }
            if (savedInstanceState != null) {
                onRestore = true
                binding.textView.post {
                    val textline = savedInstanceState.getString("textLine", "")
                    if (textline != "") {
                        val index = binding.textView.text.indexOf(textline)
                        val line = binding.textView.layout.getLineForOffset(index)
                        val y = binding.textView.layout.getLineTop(line)
                        binding.InteractiveScroll.scrollY = y
                    }
                    if (autoscroll) {
                        startAutoScroll()
                    }
                }
            } else {
                if (autoscroll) {
                    startAutoScroll()
                }
            }
        } catch (t: Throwable) {
            error()
        }
    }

    private fun error() {
        val ssb = SpannableStringBuilder(resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch))
        ssb.setSpan(StyleSpan(Typeface.BOLD), 0, resources.getString(by.carkva_gazeta.malitounik.R.string.error_ch).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textView.text = ssb
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
                invalidateOptionsMenu()
            }
        }
    }

    private fun stopAutoStartScroll() {
        autoStartScrollJob?.cancel()
    }

    private fun startProcent(delayTime: Long = 1000) {
        procentJob?.cancel()
        procentJob = CoroutineScope(Dispatchers.Main).launch {
            delay(delayTime)
            bindingprogress.progress.visibility = View.GONE
            bindingprogress.brighess.visibility = View.GONE
            bindingprogress.fontSize.visibility = View.GONE
        }
    }

    private fun stopAutoScroll(delayDisplayOff: Boolean = true, saveAutoScroll: Boolean = true) {
        if (autoScrollJob?.isActive == true) {
            if (saveAutoScroll) {
                val prefEditor: Editor = k.edit()
                prefEditor.putBoolean("autoscroll", false)
                prefEditor.apply()
            }
            binding.actionMinus.visibility = View.GONE
            binding.actionPlus.visibility = View.GONE
            val animation = AnimationUtils.loadAnimation(baseContext, by.carkva_gazeta.malitounik.R.anim.alphaout)
            binding.actionMinus.animation = animation
            binding.actionPlus.animation = animation
            autoScrollJob?.cancel()
            binding.textView.setTextIsSelectable(true)
            if (!k.getBoolean("scrinOn", false) && delayDisplayOff) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(60000)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }

    private fun startAutoScroll() {
        if (diffScroll != 0) {
            val prefEditor: Editor = k.edit()
            prefEditor.putBoolean("autoscroll", true)
            prefEditor.apply()
            binding.actionMinus.visibility = View.VISIBLE
            binding.actionPlus.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(baseContext, by.carkva_gazeta.malitounik.R.anim.alphain)
            binding.actionMinus.animation = animation
            binding.actionPlus.animation = animation
            stopAutoStartScroll()
            binding.textView.setTextIsSelectable(false)
            if (autoScrollJob?.isActive != true) {
                autoScrollJob = CoroutineScope(Dispatchers.Main).launch {
                    while (isActive) {
                        delay(spid.toLong())
                        if (!mActionDown && !MainActivity.dialogVisable) {
                            binding.InteractiveScroll.smoothScrollBy(0, 2)
                        }
                    }
                }
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            binding.InteractiveScroll.smoothScrollTo(0, 0)
            startAutoScroll()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val infl = menuInflater
        infl.inflate(by.carkva_gazeta.malitounik.R.menu.chtenia, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        autoscroll = k.getBoolean("autoscroll", false)
        if (autoscroll) {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_auto).setIcon(by.carkva_gazeta.malitounik.R.drawable.scroll_icon_on)
        } else {
            menu.findItem(by.carkva_gazeta.malitounik.R.id.action_auto).setIcon(by.carkva_gazeta.malitounik.R.drawable.scroll_icon)
        }
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_dzen_noch).isChecked = k.getBoolean("dzen_noch", false)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_utran).isChecked = k.getBoolean("utran", true)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_utran).isVisible = true
        return true
    }

    override fun onBackPressed() {
        if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else {
            if (change) {
                onSupportNavigateUp()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll(delayDisplayOff = false, saveAutoScroll = false)
        stopAutoStartScroll()
        procentJob?.cancel()
        resetTollbarJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        setTollbarTheme()
        if (fullscreenPage) hide()
        autoscroll = k.getBoolean("autoscroll", false)
        if (autoscroll && !onRestore) {
            startAutoScroll()
        }
        spid = k.getInt("autoscrollSpid", 60)
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
        if (k.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        dzenNoch = k.getBoolean("dzen_noch", false)
        val prefEditor: Editor = k.edit()
        if (id == by.carkva_gazeta.malitounik.R.id.action_dzen_noch) {
            change = true
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                prefEditor.putBoolean("dzen_noch", true)
            } else {
                prefEditor.putBoolean("dzen_noch", false)
            }
            prefEditor.apply()
            recreate()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_utran) {
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                prefEditor.putBoolean("utran", true)
            } else {
                prefEditor.putBoolean("utran", false)
            }
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_auto) {
            autoscroll = k.getBoolean("autoscroll", false)
            if (autoscroll) {
                stopAutoScroll()
            } else {
                startAutoScroll()
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

    private fun hide() {
        val actionBar = supportActionBar
        actionBar?.hide()
        CoroutineScope(Dispatchers.Main).launch {
            mHidePart2Runnable()
        }
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
        CoroutineScope(Dispatchers.Main).launch {
            mShowPart2Runnable()
        }
    }

    override fun onScroll(t: Int, oldt: Int) {
        val lineLayout = binding.textView.layout
        lineLayout?.let {
            val textForVertical = binding.textView.text.substring(binding.textView.layout.getLineStart(it.getLineForVertical(t)), binding.textView.layout.getLineEnd(it.getLineForVertical(t))).trim()
            if (textForVertical != "") firstTextPosition = textForVertical
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("fullscreen", fullscreenPage)
        outState.putBoolean("change", change)
        outState.putString("textLine", firstTextPosition)
    }
}