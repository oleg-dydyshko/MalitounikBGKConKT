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
import by.carkva_gazeta.resources.DialogBibleRazdel.DialogBibleRazdelListener
import by.carkva_gazeta.resources.databinding.ActivityBibleBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Runnable

class NovyZapavietSemuxa : AppCompatActivity(), DialogFontSizeListener, DialogBibleRazdelListener, NovyZapavietSemuxaFragment.ClicParalelListiner, NovyZapavietSemuxaFragment.ListPositionListiner, DialogBibleNatatka.DialogBibleNatatkaListiner, DialogAddZakladka.DialogAddZakladkiListiner {
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
    private lateinit var binding: ActivityBibleBinding
    private var resetTollbarJob: Job? = null
    private var resetSubTollbarJob: Job? = null

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
        set["zavet"] = 1
        set["kniga"] = kniga
        set["glava"] = binding.pager.currentItem
        set["stix"] = fierstPosition
        val gson = Gson()
        prefEditors.putString("bible_time_semuxa", gson.toJson(set))
        prefEditors.apply()
        clearEmptyPosition()
        val file = File("$filesDir/BibliaSemuxaNovyZavet/$kniga.json")
        if (BibleGlobalList.vydelenie.size == 0) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            file.writer().use {
                it.write(gson.toJson(BibleGlobalList.vydelenie))
            }
        }
        val fileZakladki = File("$filesDir/BibliaSemuxaZakladki.json")
        if (BibleGlobalList.zakladkiSemuxa.size == 0) {
            if (fileZakladki.exists()) {
                fileZakladki.delete()
            }
        } else {
            fileZakladki.writer().use {
                it.write(gson.toJson(BibleGlobalList.zakladkiSemuxa))
            }
        }
        val fileNatatki = File("$filesDir/BibliaSemuxaNatatki.json")
        if (BibleGlobalList.natatkiSemuxa.size == 0) {
            if (fileNatatki.exists()) {
                fileNatatki.delete()
            }
        } else {
            fileNatatki.writer().use {
                it.write(gson.toJson(BibleGlobalList.natatkiSemuxa))
            }
        }
        resetTollbarJob?.cancel()
        resetSubTollbarJob?.cancel()
    }

    override fun onDialogFontSize(fontSize: Float) {
        binding.pager.adapter?.notifyDataSetChanged()
    }

    override fun onComplete(glava: Int) {
        binding.pager.currentItem = glava
    }

    override fun getListPosition(position: Int) {
        fierstPosition = position
    }

    override fun setEdit(edit: Boolean) {
        setedit = edit
    }

    override fun addZakladka(color: Int) {
        val adapter = binding.pager.adapter as NovyZapavietSemuxa.MyPagerAdapter
        val fragment = adapter.getFragment(binding.pager.currentItem) as BackPressedFragment
        fragment.addZakladka(color)
    }

    override fun addNatatka() {
        val adapter = binding.pager.adapter as MyPagerAdapter
        val fragment = adapter.getFragment(binding.pager.currentItem) as BackPressedFragment
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
        binding = ActivityBibleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        kniga = intent.extras?.getInt("kniga", 0) ?: 0
        glava = intent.extras?.getInt("glava", 0) ?: 0
        if (intent.extras?.containsKey("stix") == true) {
            fierstPosition = intent.extras?.getInt("stix", 0) ?: 0
            trak = true
        }
        BibleGlobalList.mListGlava = 0
        binding.pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
        for (i in 0 until binding.pagerTabStrip.childCount) {
            val nextChild = binding.pagerTabStrip.getChildAt(i)
            if (nextChild is TextView) {
                nextChild.typeface = TextViewRobotoCondensed.createFont(Typeface.NORMAL)
            }
        }
        val adapterViewPager = MyPagerAdapter(supportFragmentManager)
        binding.pager.adapter = adapterViewPager
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                BibleGlobalList.mListGlava = position
                men = VybranoeBibleList.checkVybranoe(this@NovyZapavietSemuxa, kniga, position)
                if (glava != position) fierstPosition = 0
                invalidateOptionsMenu()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        when (kniga) {
            0 -> {
                title = "Паводле Мацьвея"
                fullglav = 28
            }
            1 -> {
                title = "Паводле Марка"
                fullglav = 16
            }
            2 -> {
                title = "Паводле Лукаша"
                fullglav = 24
            }
            3 -> {
                title = "Паводле Яна"
                fullglav = 21
            }
            4 -> {
                title = "Дзеі Апосталаў"
                fullglav = 28
            }
            5 -> {
                title = "Якава"
                fullglav = 5
            }
            6 -> {
                title = "1-е Пятра"
                fullglav = 5
            }
            7 -> {
                title = "2-е Пятра"
                fullglav = 3
            }
            8 -> {
                title = "1-е Яна Багаслова"
                fullglav = 5
            }
            9 -> {
                title = "2-е Яна Багаслова"
                fullglav = 1
            }
            10 -> {
                title = "3-е Яна Багаслова"
                fullglav = 1
            }
            11 -> {
                title = "Юды"
                fullglav = 1
            }
            12 -> {
                title = "Да Рымлянаў"
                fullglav = 16
            }
            13 -> {
                title = "1-е да Карынфянаў"
                fullglav = 16
            }
            14 -> {
                title = "2-е да Карынфянаў"
                fullglav = 13
            }
            15 -> {
                title = "Да Галятаў"
                fullglav = 6
            }
            16 -> {
                title = "Да Эфэсянаў"
                fullglav = 6
            }
            17 -> {
                title = "Да Піліпянаў"
                fullglav = 4
            }
            18 -> {
                title = "Да Каласянаў"
                fullglav = 4
            }
            19 -> {
                title = "1-е да Фесаланікійцаў"
                fullglav = 5
            }
            20 -> {
                title = "2-е да Фесаланікійцаў"
                fullglav = 3
            }
            21 -> {
                title = "1-е да Цімафея"
                fullglav = 6
            }
            22 -> {
                title = "2-е да Цімафея"
                fullglav = 4
            }
            23 -> {
                title = "Да Ціта"
                fullglav = 3
            }
            24 -> {
                title = "Да Філімона"
                fullglav = 1
            }
            25 -> {
                title = "Да Габрэяў"
                fullglav = 13
            }
            26 -> {
                title = "Адкрыцьцё (Апакаліпсіс)"
                fullglav = 22
            }
        }
        men = VybranoeBibleList.checkVybranoe(this, kniga, glava)
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
        binding.pager.currentItem = glava
        requestedOrientation = if (k.getBoolean("orientation", false)) {
            orientation
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        val file = File("$filesDir/BibliaSemuxaNovyZavet/$kniga.json")
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
        binding.subtitleToolbar.setOnClickListener {
            val layoutParams = binding.toolbar.layoutParams
            if (binding.subtitleToolbar.isSelected) {
                resetSubTollbarJob?.cancel()
                resetSubTollbar(layoutParams)
            } else {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.subtitleToolbar.isSingleLine = false
                binding.subtitleToolbar.isSelected = true
                resetSubTollbarJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(5000)
                    resetSubTollbar(layoutParams)
                }
            }
        }
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = getString(by.carkva_gazeta.malitounik.R.string.novy_zapaviet)
        binding.subtitleToolbar.text = title
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

    private fun resetSubTollbar(layoutParams: ViewGroup.LayoutParams) {
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            layoutParams.height = actionBarHeight
        }
        binding.subtitleToolbar.isSelected = false
        binding.subtitleToolbar.isSingleLine = true
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
            binding.scroll.visibility = View.GONE
            binding.pager.visibility = View.VISIBLE
            binding.subtitleToolbar.visibility = View.VISIBLE
            binding.titleToolbar.text = getString(by.carkva_gazeta.malitounik.R.string.novy_zapaviet)
            binding.subtitleToolbar.text = title
            paralel = false
            invalidateOptionsMenu()
        } else if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else if (BibleGlobalList.mPedakVisable) {
            val adapter = binding.pager.adapter as MyPagerAdapter
            val fragment = adapter.getFragment(binding.pager.currentItem) as BackPressedFragment
            fragment.onBackPressedFragment()
        } else {
            if (setedit || checkSetDzenNoch) {
                onSupportNavigateUp()
            } else {
                super.onBackPressed()
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
            men = VybranoeBibleList.setVybranoe(this, title, kniga, BibleGlobalList.mListGlava, true)
            if (men) {
                MainActivity.toastView(this, getString(by.carkva_gazeta.malitounik.R.string.addVybranoe))
                if (!VybranoeBibleList.checkVybranoe("1")) {
                    MenuVybranoe.vybranoe.add(0, VybranoeData(Bogashlugbovya.vybranoeIndex(), "1", getString(by.carkva_gazeta.malitounik.R.string.title_biblia)))
                    val gson = Gson()
                    val file = File("$filesDir/Vybranoe.json")
                    file.writer().use {
                        it.write(gson.toJson(MenuVybranoe.vybranoe))
                    }
                }
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
            val dialogBibleRazdel = DialogBibleRazdel.getInstance(fullglav)
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
        if (k.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, uiAnimationDelay)
    }

    override fun setOnClic(cytanneParalelnye: String, cytanneSours: String) {
        paralel = true
        this.cytanneParalelnye = cytanneParalelnye
        this.cytanneSours = cytanneSours
        val pm = ParalelnyeMesta()
        binding.conteiner.removeAllViewsInLayout()
        val arrayList = pm.paralel(this@NovyZapavietSemuxa, this.cytanneSours, this.cytanneParalelnye, true)
        for (textView in arrayList) {
            binding.conteiner.addView(textView)
        }
        binding.scroll.visibility = View.VISIBLE
        binding.pager.visibility = View.GONE
        binding.titleToolbar.text = resources.getString(by.carkva_gazeta.malitounik.R.string.paralel_smoll, cytanneSours)
        binding.subtitleToolbar.visibility = View.GONE
        invalidateOptionsMenu()
    }

    private inner class MyPagerAdapter(fragmentManager: FragmentManager) : SmartFragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            var fullglav = 1
            when (kniga) {
                0, 4 -> fullglav = 28
                1, 13, 12 -> fullglav = 16
                2 -> fullglav = 24
                3 -> fullglav = 21
                5, 19, 8, 6 -> fullglav = 5
                7, 23, 20 -> fullglav = 3
                9, 24, 11, 10 -> {
                }
                14, 25 -> fullglav = 13
                15, 21, 16 -> fullglav = 6
                17, 22, 18 -> fullglav = 4
                26 -> fullglav = 22
            }
            return fullglav
        }

        override fun getItem(position: Int): BackPressedFragment {
            for (i in 0 until count) {
                if (position == i) {
                    val pazicia: Int = if (trak) {
                        if (glava != i) 0 else fierstPosition
                    } else 0
                    return NovyZapavietSemuxaFragment.newInstance(i, kniga, pazicia)
                }
            }
            return NovyZapavietSemuxaFragment.newInstance(0, kniga, 1)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return resources.getString(by.carkva_gazeta.malitounik.R.string.razdzel) + " " + (position + 1)
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

    companion object {
        var fierstPosition = 0
    }
}