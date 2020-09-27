package by.carkva_gazeta.resources

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.malitounik.DialogFontSize.DialogFontSizeListener
import by.carkva_gazeta.resources.DialogBibleRazdel.Companion.getInstance
import by.carkva_gazeta.resources.DialogBibleRazdel.DialogBibleRazdelListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_bible.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class StaryZapavietSinaidal : AppCompatActivity(), DialogFontSizeListener, DialogBibleRazdelListener, StaryZapavietSinaidalFragment.ClicParalelListiner, StaryZapavietSinaidalFragment.ListPositionListiner, DialogBibleNatatka.DialogBibleNatatkaListiner, DialogAddZakladka.DialogAddZakladkiListiner {
    private val mHideHandler = Handler(Looper.getMainLooper())

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
        val actionBar = supportActionBar
        actionBar?.show()
    }
    private var fullscreenPage = false
    private var trak = false
    private var paralel = false
    private var fullglav = 0
    private var kniga = 0
    private var glava = 0
    private lateinit var k: SharedPreferences
    private var dzenNoch = false
    private var dialog = true
    private var cytanneSours = ""
    private var cytanneParalelnye = ""
    private var setedit = false
    private var checkSetDzenNoch = false
    private var title = ""
    private var men = true
    private val uiAnimationDelay: Long = 300
    private val orientation: Int
        get() {
            return MainActivity.getOrientation(this)
        }

    private fun clearEmptyPosition() {
        val remove = ArrayList<ArrayList<Int>>()
        for (i in BibleGlobalList.vydelenie.indices) {
            var posrem = true
            for (e in 1 until BibleGlobalList.vydelenie[i].size) {
                if (BibleGlobalList.vydelenie[i][e] == 1) {
                    posrem = false
                    break
                }
            }
            if (posrem) {
                remove.add(BibleGlobalList.vydelenie[i])
            }
        }
        BibleGlobalList.vydelenie.removeAll(remove)
    }

    override fun onPause() {
        super.onPause()
        val prefEditors = k.edit()
        // Формат: Завет(1-Новый, 0-Старый) : Книга : Глава : Стих
        val set = ArrayMap<String, Int>()
        set["zavet"] = 0
        set["kniga"] = kniga
        set["glava"] = pager.currentItem
        set["stix"] = fierstPosition
        val gson = Gson()
        prefEditors.putString("bible_time_sinodal", gson.toJson(set))
        prefEditors.apply()
        clearEmptyPosition()
        val file = File("$filesDir/BibliaSinodalStaryZavet/$kniga.json")
        if (BibleGlobalList.vydelenie.size == 0) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            val outputStream = FileWriter(file)
            outputStream.write(gson.toJson(BibleGlobalList.vydelenie))
            outputStream.close()
        }
        val fileZakladki = File("$filesDir/BibliaSinodalZakladki.json")
        if (BibleGlobalList.zakladkiSinodal.size == 0) {
            if (fileZakladki.exists()) {
                fileZakladki.delete()
            }
        } else {
            val outputStream = FileWriter(fileZakladki)
            outputStream.write(gson.toJson(BibleGlobalList.zakladkiSinodal))
            outputStream.close()
        }
        val fileNatatki = File("$filesDir/BibliaSinodalNatatki.json")
        if (BibleGlobalList.natatkiSinodal.size == 0) {
            if (fileNatatki.exists()) {
                fileNatatki.delete()
            }
        } else {
            val outputStream = FileWriter(fileNatatki)
            outputStream.write(gson.toJson(BibleGlobalList.natatkiSinodal))
            outputStream.close()
        }
    }

    override fun onDialogFontSizePositiveClick() {
        pager.adapter?.notifyDataSetChanged()
    }

    override fun onComplete(glava: Int) {
        pager.currentItem = glava
    }

    override fun getListPosition(position: Int) {
        fierstPosition = position
    }

    override fun setEdit(edit: Boolean) {
        setedit = edit
    }

    override fun addZakladka(color: Int) {
        val adapter = pager.adapter as StaryZapavietSinaidal.MyPagerAdapter
        val fragment = adapter.getFragment(pager.currentItem) as BackPressedFragment
        fragment.addZakladka(color)
    }

    override fun addNatatka() {
        val adapter = pager.adapter as MyPagerAdapter
        val fragment = adapter.getFragment(pager.currentItem) as BackPressedFragment
        fragment.addNatatka()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        if (savedInstanceState != null) {
            checkSetDzenNoch = savedInstanceState.getBoolean("checkSetDzenNoch")
            setedit = savedInstanceState.getBoolean("setedit")
        }
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bible)
        kniga = intent.extras?.getInt("kniga", 0) ?: 0
        glava = intent.extras?.getInt("glava", 0) ?: 0
        if (intent.extras?.containsKey("stix") == true) {
            fierstPosition = intent.extras?.getInt("stix", 0) ?: 0
            trak = true
        }
        BibleGlobalList.mListGlava = 0
        pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
        for (i in 0 until pagerTabStrip.childCount) {
            val nextChild = pagerTabStrip.getChildAt(i)
            if (nextChild is TextView) {
                nextChild.typeface = TextViewRobotoCondensed.createFont(Typeface.NORMAL)
            }
        }
        val adapterViewPager: SmartFragmentStatePagerAdapter = MyPagerAdapter(supportFragmentManager)
        pager.adapter = adapterViewPager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (dzenNoch) {
                window.statusBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
                window.navigationBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary_text)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimaryDark)
                window.navigationBarColor = ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimaryDark)
            }
        }
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                BibleGlobalList.mListGlava = position
                men = MyBibleList.checkVybranoe(this@StaryZapavietSinaidal, kniga, position, 2)
                if (glava != position) NovyZapavietSemuxa.fierstPosition = 0
                invalidateOptionsMenu()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        when (kniga) {
            0 -> {
                title = "Бытие"
                fullglav = 50
            }
            1 -> {
                title = "Исход"
                fullglav = 40
            }
            2 -> {
                title = "Левит"
                fullglav = 27
            }
            3 -> {
                title = "Числа"
                fullglav = 36
            }
            4 -> {
                title = "Второзаконие"
                fullglav = 34
            }
            5 -> {
                title = "Иисуса Навина"
                fullglav = 24
            }
            6 -> {
                title = "Судей израилевых"
                fullglav = 21
            }
            7 -> {
                title = "Руфи"
                fullglav = 4
            }
            8 -> {
                title = "1-я Царств"
                fullglav = 31
            }
            9 -> {
                title = "2-я Царств"
                fullglav = 24
            }
            10 -> {
                title = "3-я Царств"
                fullglav = 22
            }
            11 -> {
                title = "4-я Царств"
                fullglav = 25
            }
            12 -> {
                title = "1-я Паралипоменон"
                fullglav = 29
            }
            13 -> {
                title = "2-я Паралипоменон"
                fullglav = 37
            }
            14 -> {
                title = "1-я Ездры"
                fullglav = 10
            }
            15 -> {
                title = "Неемии"
                fullglav = 13
            }
            16 -> {
                title = "2-я Ездры"
                fullglav = 9
            }
            17 -> {
                title = "Товита"
                fullglav = 14
            }
            18 -> {
                title = "Иудифи"
                fullglav = 16
            }
            19 -> {
                title = "Есфири"
                fullglav = 10
            }
            20 -> {
                title = "Иова"
                fullglav = 42
            }
            21 -> {
                title = "Псалтирь"
                fullglav = 151
            }
            22 -> {
                title = "Притчи Соломона"
                fullglav = 31
            }
            23 -> {
                title = "Екклезиаста"
                fullglav = 12
            }
            24 -> {
                title = "Песнь песней Соломона"
                fullglav = 8
            }
            25 -> {
                title = "Премудрости Соломона"
                fullglav = 19
            }
            26 -> {
                title = "Премудрости Иисуса, сына Сирахова"
                fullglav = 51
            }
            27 -> {
                title = "Исаии"
                fullglav = 66
            }
            28 -> {
                title = "Иеремии"
                fullglav = 52
            }
            29 -> {
                title = "Плач Иеремии"
                fullglav = 5
            }
            30 -> {
                title = "Послание Иеремии"
                fullglav = 1
            }
            31 -> {
                title = "Варуха"
                fullglav = 5
            }
            32 -> {
                title = "Иезекииля"
                fullglav = 48
            }
            33 -> {
                title = "Даниила"
                fullglav = 14
            }
            34 -> {
                title = "Осии"
                fullglav = 14
            }
            35 -> {
                title = "Иоиля"
                fullglav = 3
            }
            36 -> {
                title = "Амоса"
                fullglav = 9
            }
            37 -> {
                title = "Авдия"
                fullglav = 1
            }
            38 -> {
                title = "Ионы"
                fullglav = 4
            }
            39 -> {
                title = "Михея"
                fullglav = 7
            }
            40 -> {
                title = "Наума"
                fullglav = 3
            }
            41 -> {
                title = "Аввакума"
                fullglav = 3
            }
            42 -> {
                title = "Сафонии"
                fullglav = 3
            }
            43 -> {
                title = "Аггея"
                fullglav = 2
            }
            44 -> {
                title = "Захарии"
                fullglav = 14
            }
            45 -> {
                title = "Малахии"
                fullglav = 4
            }
            46 -> {
                title = "1-я Маккавейская"
                fullglav = 16
            }
            47 -> {
                title = "2-я Маккавейская"
                fullglav = 15
            }
            48 -> {
                title = "3-я Маккавейская"
                fullglav = 7
            }
            49 -> {
                title = "3-я Ездры"
                fullglav = 16
            }
        }
        men = MyBibleList.checkVybranoe(this, kniga, glava, 2)
        if (savedInstanceState != null) {
            dialog = savedInstanceState.getBoolean("dialog")
            paralel = savedInstanceState.getBoolean("paralel")
            cytanneSours = savedInstanceState.getString("cytanneSours") ?: ""
            cytanneParalelnye = savedInstanceState.getString("cytanneParalelnye") ?: ""
            fullscreenPage = savedInstanceState.getBoolean("fullscreen")
            if (paralel) {
                setOnClic(cytanneParalelnye, cytanneSours)
            }
        }
        pager.currentItem = glava
        requestedOrientation = if (k.getBoolean("orientation", false)) {
            orientation
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        val file = File("$filesDir/BibliaSinodalStaryZavet/$kniga.json")
        if (file.exists()) {
            val inputStream = FileReader(file)
            val reader = BufferedReader(inputStream)
            val gson = Gson()
            val type = object : TypeToken<ArrayList<ArrayList<Int?>?>?>() {}.type
            BibleGlobalList.vydelenie = gson.fromJson(reader.readText(), type)
            inputStream.close()
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
        title_toolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title_toolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.stsinaidal)
        subtitle_toolbar.text = title
        if (dzenNoch) {
            toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
            toolbar.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("dialog", dialog)
        outState.putBoolean("paralel", paralel)
        outState.putString("cytanneSours", cytanneSours)
        outState.putString("cytanneParalelnye", cytanneParalelnye)
        outState.putBoolean("fullscreen", fullscreenPage)
        outState.putBoolean("checkSetDzenNoch", checkSetDzenNoch)
        outState.putBoolean("setedit", setedit)
    }

    override fun onBackPressed() {
        if (paralel) {
            scroll.visibility = View.GONE
            pager.visibility = View.VISIBLE
            subtitle_toolbar.visibility = View.VISIBLE
            title_toolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.stsinaidal)
            subtitle_toolbar.text = title
            paralel = false
            invalidateOptionsMenu()
        } else if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else if (BibleGlobalList.mPedakVisable) {
            val adapter = pager.adapter as MyPagerAdapter
            val fragment = adapter.getFragment(pager.currentItem) as BackPressedFragment
            fragment.onBackPressedFragment()
        } else {
            if (setedit) {
                setResult(500)
                finish()
            } else {
                if (checkSetDzenNoch) onSupportNavigateUp() else super.onBackPressed()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_glava).isVisible = !paralel
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_orientation).isChecked = k.getBoolean("orientation", false)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.action_dzen_noch).isChecked = k.getBoolean("dzen_noch", false)
        val itemVybranoe: MenuItem = menu.findItem(by.carkva_gazeta.malitounik.R.id.action_vybranoe)
        if (men) {
            itemVybranoe.icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_on)
            itemVybranoe.title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe_del)
        } else {
            itemVybranoe.icon = ContextCompat.getDrawable(this, by.carkva_gazeta.malitounik.R.drawable.star_big_off)
            itemVybranoe.title = resources.getString(by.carkva_gazeta.malitounik.R.string.vybranoe)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val prefEditors = k.edit()
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (id == by.carkva_gazeta.malitounik.R.id.action_vybranoe) {
            checkSetDzenNoch = true
            men = MyBibleList.setVybranoe(this, title, kniga, BibleGlobalList.mListGlava, bibleName = 2)
            if (men) {
                MainActivity.toastView(this, getString(by.carkva_gazeta.malitounik.R.string.addVybranoe))
            }
            invalidateOptionsMenu()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_dzen_noch) {
            checkSetDzenNoch = true
            val prefEditor = k.edit()
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                prefEditor.putBoolean("dzen_noch", true)
            } else {
                prefEditor.putBoolean("dzen_noch", false)
            }
            prefEditor.apply()
            recreate()
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_orientation) {
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                requestedOrientation = orientation
                prefEditors.putBoolean("orientation", true)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                prefEditors.putBoolean("orientation", false)
            }
        }
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (id == by.carkva_gazeta.malitounik.R.id.action_glava) {
            val dialogBibleRazdel = getInstance(fullglav)
            dialogBibleRazdel.show(supportFragmentManager, "full_glav")
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
        prefEditors.apply()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        setTollbarTheme()
        if (fullscreenPage) hide()
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val infl = menuInflater
        infl.inflate(by.carkva_gazeta.malitounik.R.menu.biblia, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        return true
    }

    private fun hide() {
        val actionBar = supportActionBar
        actionBar?.hide()
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, uiAnimationDelay)
    }

    @Suppress("DEPRECATION")
    private fun show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            val controller = window.insetsController
            controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            //controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, uiAnimationDelay)
    }

    override fun setOnClic(cytanneParalelnye: String?, cytanneSours: String?) {
        paralel = true
        this.cytanneParalelnye = cytanneParalelnye ?: ""
        this.cytanneSours = cytanneSours ?: ""
        val pm = ParalelnyeMesta()
        conteiner.removeAllViewsInLayout()
        val arrayList = pm.paralel(this@StaryZapavietSinaidal, this.cytanneSours, this.cytanneParalelnye, false)
        for (textView in arrayList) {
            conteiner.addView(textView)
        }
        scroll.visibility = View.VISIBLE
        pager.visibility = View.GONE
        title_toolbar.text = resources.getString(by.carkva_gazeta.malitounik.R.string.paralel_smoll, cytanneSours)
        subtitle_toolbar.visibility = View.GONE
        invalidateOptionsMenu()
    }

    private inner class MyPagerAdapter(fragmentManager: FragmentManager) : SmartFragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            var fullglav = 1
            when (kniga) {
                0 -> fullglav = 50
                1 -> fullglav = 40
                2 -> fullglav = 27
                3 -> fullglav = 36
                4 -> fullglav = 34
                5 -> fullglav = 24
                6 -> fullglav = 21
                7 -> fullglav = 4
                8 -> fullglav = 31
                9 -> fullglav = 24
                10 -> fullglav = 22
                11 -> fullglav = 25
                12 -> fullglav = 29
                13 -> fullglav = 37
                14 -> fullglav = 10
                15 -> fullglav = 13
                16 -> fullglav = 9
                17 -> fullglav = 14
                18 -> fullglav = 16
                19 -> fullglav = 10
                20 -> fullglav = 42
                21 -> fullglav = 151
                22 -> fullglav = 31
                23 -> fullglav = 12
                24 -> fullglav = 8
                25 -> fullglav = 19
                26 -> fullglav = 51
                27 -> fullglav = 66
                28 -> fullglav = 52
                29 -> fullglav = 5
                30, 37 -> {
                }
                31 -> fullglav = 5
                32 -> fullglav = 48
                33 -> fullglav = 14
                34 -> fullglav = 14
                35 -> fullglav = 3
                36 -> fullglav = 9
                38 -> fullglav = 4
                39 -> fullglav = 7
                40 -> fullglav = 3
                41 -> fullglav = 3
                42 -> fullglav = 3
                43 -> fullglav = 2
                44 -> fullglav = 14
                45 -> fullglav = 4
                46 -> fullglav = 16
                47 -> fullglav = 15
                48 -> fullglav = 7
                49 -> fullglav = 16
            }
            return fullglav
        }

        override fun getItem(position: Int): BackPressedFragment {
            for (i in 0 until count) {
                if (position == i) {
                    val pazicia: Int = if (trak) {
                        if (glava != i) 0 else fierstPosition
                    } else 0
                    return StaryZapavietSinaidalFragment.newInstance(i, kniga, pazicia)
                }
            }
            return StaryZapavietSinaidalFragment.newInstance(0, kniga, 1)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if (kniga == 21) resources.getString(by.carkva_gazeta.malitounik.R.string.psinaidal) + " " + (position + 1) else resources.getString(by.carkva_gazeta.malitounik.R.string.rsinaidal) + " " + (position + 1)
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

    companion object {
        var fierstPosition = 0
    }
}