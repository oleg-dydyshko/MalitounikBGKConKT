package by.carkva_gazeta.resources

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.malitounik.DialogFontSize.DialogFontSizeListener
import by.carkva_gazeta.resources.DialogBibleRazdel.Companion.getInstance
import by.carkva_gazeta.resources.DialogBibleRazdel.DialogBibleRazdelListener
import by.carkva_gazeta.resources.databinding.ActivityBibleBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class StaryZapavietSinaidal : AppCompatActivity(), DialogFontSizeListener, DialogBibleRazdelListener, StaryZapavietSinaidalFragment.ClicParalelListiner, StaryZapavietSinaidalFragment.ListPositionListiner, DialogBibleNatatka.DialogBibleNatatkaListiner, DialogAddZakladka.DialogAddZakladkiListiner {

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
    private lateinit var binding: ActivityBibleBinding
    private var resetTollbarJob: Job? = null

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
        set["glava"] = binding.pager.currentItem
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
            file.writer().use {
                it.write(gson.toJson(BibleGlobalList.vydelenie))
            }
        }
        val fileZakladki = File("$filesDir/BibliaSinodalZakladki.json")
        if (BibleGlobalList.zakladkiSinodal.size == 0) {
            if (fileZakladki.exists()) {
                fileZakladki.delete()
            }
        } else {
            fileZakladki.writer().use {
                it.write(gson.toJson(BibleGlobalList.zakladkiSinodal))
            }
        }
        val fileNatatki = File("$filesDir/BibliaSinodalNatatki.json")
        if (BibleGlobalList.natatkiSinodal.size == 0) {
            if (fileNatatki.exists()) {
                fileNatatki.delete()
            }
        } else {
            fileNatatki.writer().use {
                it.write(gson.toJson(BibleGlobalList.natatkiSinodal))
            }
        }
        resetTollbarJob?.cancel()
    }

    override fun onDialogFontSize(fontSize: Float) {
        binding.pager.adapter?.notifyDataSetChanged()
    }

    override fun onComplete(glava: Int) {
        binding.pager.setCurrentItem(glava, false)
    }

    override fun getListPosition(position: Int) {
        fierstPosition = position
    }

    override fun setEdit(edit: Boolean) {
        setedit = edit
    }

    override fun addZakladka(color: Int) {
        val fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.currentItem) as BackPressedFragment
        fragment.addZakladka(color)
    }

    override fun addNatatka() {
        val fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.currentItem) as BackPressedFragment
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
        val adapterViewPager = MyPagerAdapter(this)
        binding.pager.adapter = adapterViewPager
        TabLayoutMediator(binding.tabLayout, binding.pager, false) { tab, position ->
            tab.text = if (kniga == 21) resources.getString(by.carkva_gazeta.malitounik.R.string.psinaidal) + " " + (position + 1) else resources.getString(by.carkva_gazeta.malitounik.R.string.rsinaidal) + " " + (position + 1)
        }.attach()
        binding.pager.offscreenPageLimit = 3
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                BibleGlobalList.mListGlava = position
                men = VybranoeBibleList.checkVybranoe(this@StaryZapavietSinaidal, kniga, position, 2)
                if (glava != position) fierstPosition = 0
                invalidateOptionsMenu()
            }
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
        men = VybranoeBibleList.checkVybranoe(this, kniga, glava, 2)
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
        binding.pager.setCurrentItem(glava, false)
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
        binding.titleToolbar.setOnClickListener {
            fullTextTollbar()
        }
        binding.subtitleToolbar.setOnClickListener {
            fullTextTollbar()
        }
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.stsinaidal)
        binding.subtitleToolbar.text = title
        if (dzenNoch) {
            binding.toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
        }
    }

    private fun fullTextTollbar() {
        val layoutParams = binding.toolbar.layoutParams
        resetTollbarJob?.cancel()
        if (binding.titleToolbar.isSelected) {
            resetTollbar(layoutParams)
        } else {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.titleToolbar.isSingleLine = false
            binding.subtitleToolbar.isSingleLine = false
            binding.titleToolbar.isSelected = true
            resetTollbarJob = CoroutineScope(Dispatchers.Main).launch {
                delay(5000)
                resetTollbar(layoutParams)
            }
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
            binding.tabLayout.visibility = View.VISIBLE
            binding.subtitleToolbar.visibility = View.VISIBLE
            binding.titleToolbar.text = resources.getText(by.carkva_gazeta.malitounik.R.string.stsinaidal)
            binding.subtitleToolbar.text = title
            paralel = false
            invalidateOptionsMenu()
        } else if (fullscreenPage) {
            fullscreenPage = false
            show()
        } else if (BibleGlobalList.mPedakVisable) {
            val fragment = supportFragmentManager.findFragmentByTag("f" + binding.pager.currentItem) as BackPressedFragment
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
            men = VybranoeBibleList.setVybranoe(this, title, kniga, BibleGlobalList.mListGlava, bibleName = 2)
            if (men) {
                MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.addVybranoe))
                if (!VybranoeBibleList.checkVybranoe("2")) {
                    MenuVybranoe.vybranoe.add(0, VybranoeData(Bogashlugbovya.vybranoeIndex(), "2", getString(by.carkva_gazeta.malitounik.R.string.bsinaidal)))
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

    override fun setOnClic(cytanneParalelnye: String?, cytanneSours: String?) {
        paralel = true
        this.cytanneParalelnye = cytanneParalelnye ?: ""
        this.cytanneSours = cytanneSours ?: ""
        val pm = ParalelnyeMesta()
        binding.conteiner.text = pm.paralel(this@StaryZapavietSinaidal, this.cytanneSours, this.cytanneParalelnye, false).trim()
        binding.scroll.visibility = View.VISIBLE
        binding.pager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.titleToolbar.text = resources.getString(by.carkva_gazeta.malitounik.R.string.paralel_smoll, cytanneSours)
        binding.subtitleToolbar.visibility = View.GONE
        invalidateOptionsMenu()
    }

    private inner class MyPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun onBindViewHolder(holder: FragmentViewHolder, position: Int, payloads: MutableList<Any>) {
            val fragment = supportFragmentManager.findFragmentByTag("f" + holder.itemId) as? StaryZapavietSinaidalFragment
            fragment?.upDateListView() ?: super.onBindViewHolder(holder, position, payloads)
        }

        override fun getItemCount(): Int {
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

        override fun createFragment(position: Int): BackPressedFragment {
            for (i in 0 until itemCount) {
                if (position == i) {
                    val pazicia: Int = if (trak) {
                        if (glava != i) 0 else fierstPosition
                    } else 0
                    return StaryZapavietSinaidalFragment.newInstance(i, kniga, pazicia)
                }
            }
            return StaryZapavietSinaidalFragment.newInstance(0, kniga, 1)
        }
    }

    companion object {
        var fierstPosition = 0
    }
}