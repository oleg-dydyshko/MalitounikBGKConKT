package by.carkva_gazeta.resources

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.resources.DialogDeliteAllZakladkiINatatki.DialogDeliteAllZakladkiINatatkiListener
import by.carkva_gazeta.resources.DialogZakladkaDelite.ZakladkaDeliteListiner
import com.google.gson.Gson
import com.woxthebox.draglistview.DragItem
import com.woxthebox.draglistview.DragItemAdapter
import com.woxthebox.draglistview.DragListView
import com.woxthebox.draglistview.swipe.ListSwipeHelper
import com.woxthebox.draglistview.swipe.ListSwipeItem
import kotlinx.android.synthetic.main.bible_zakladki.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class BibleZakladki : AppCompatActivity(), ZakladkaDeliteListiner, DialogDeliteAllZakladkiINatatkiListener {
    private lateinit var adapter: ItemAdapter //BibleZakladkiListAdaprer
    private var data = ArrayList<BibleZakladkiData>()
    private var semuxa = 1
    private var dzenNoch = false
    private var mLastClickTime: Long = 0
    override fun fileAllNatatkiAlboZakladki(semuxa: Int) {
        if (semuxa == 1) {
            data.removeAll(data)
            adapter.notifyDataSetChanged()
            val fileZakladki = File("$filesDir/BibliaSemuxaZakladki.json")
            if (fileZakladki.exists()) {
                fileZakladki.delete()
            }
        }
        if (semuxa == 2) {
            data.removeAll(data)
            adapter.notifyDataSetChanged()
            val fileZakladki = File("$filesDir/BibliaSinodalZakladki.json")
            if (fileZakladki.exists()) {
                fileZakladki.delete()
            }
        }
        help.visibility = View.VISIBLE
        drag_list_view.visibility = View.GONE
        invalidateOptionsMenu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        val k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        dzenNoch = k.getBoolean("dzen_noch", false)
        if (dzenNoch) setTheme(by.carkva_gazeta.malitounik.R.style.AppCompatDark)
        if (k.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.bible_zakladki)
        semuxa = intent.getIntExtra("semuxa", 1)
        if (semuxa == 1) {
            data = BibleGlobalList.zakladkiSemuxa
        }
        if (semuxa == 2) {
            data = BibleGlobalList.zakladkiSinodal
        }
        adapter = ItemAdapter(data, by.carkva_gazeta.malitounik.R.layout.list_item, by.carkva_gazeta.malitounik.R.id.image, false) //BibleZakladkiListAdaprer(this, data)
        drag_list_view.recyclerView.isVerticalScrollBarEnabled = false
        drag_list_view.setLayoutManager(LinearLayoutManager(this))
        drag_list_view.setAdapter(adapter, false)
        drag_list_view.setCanDragHorizontally(false)
        drag_list_view.setCanDragVertically(true)
        drag_list_view.setCustomDragItem(MyDragItem(this, by.carkva_gazeta.malitounik.R.layout.list_item))
        drag_list_view.setSwipeListener(object : ListSwipeHelper.OnSwipeListenerAdapter() {
            override fun onItemSwipeStarted(item: ListSwipeItem) {
            }

            override fun onItemSwipeEnded(item: ListSwipeItem, swipedDirection: ListSwipeItem.SwipeDirection) {
                if (swipedDirection == ListSwipeItem.SwipeDirection.LEFT) {
                    val adapterItem = item.tag as BibleZakladkiData
                    val position: Int = drag_list_view.adapter.getPositionForItem(adapterItem)
                    val t1 = data[position].data.indexOf("\n\n")
                    val t2: Int
                    t2 = if (semuxa == 1) data[position].data.indexOf(". ", t1) else data[position].data.indexOf(" ", t1)
                    val delite = DialogZakladkaDelite.getInstance(position, data[position].data.substring(0, t1) + getString(by.carkva_gazeta.malitounik.R.string.stix_by) + " " + data[position].data.substring(t1 + 2, t2), semuxa, true)
                    delite.show(supportFragmentManager, "zakladka_delite")
                }
            }
        })
        drag_list_view.setDragListListener(object : DragListView.DragListListener {
            override fun onItemDragStarted(position: Int) {
            }

            override fun onItemDragging(itemPosition: Int, x: Float, y: Float) {
            }

            override fun onItemDragEnded(fromPosition: Int, toPosition: Int) {
                if (fromPosition != toPosition) {
                    val gson = Gson()
                    if (semuxa == 1) {
                        val fileZakladki = File("$filesDir/BibliaSemuxaZakladki.json")
                        if (data.size == 0) {
                            if (fileZakladki.exists()) {
                                fileZakladki.delete()
                            }
                        } else {
                            fileZakladki.writer().use {
                                it.write(gson.toJson(data))
                            }
                        }
                    }
                    if (semuxa == 2) {
                        val fileZakladki = File("$filesDir/BibliaSinodalZakladki.json")
                        if (data.size == 0) {
                            if (fileZakladki.exists()) {
                                fileZakladki.delete()
                            }
                        } else {
                            fileZakladki.writer().use {
                                it.write(gson.toJson(data))
                            }
                        }
                    }
                }
            }
        })
        if (data.size == 0) {
            help.visibility = View.VISIBLE
            drag_list_view.visibility = View.GONE
        }
        //if (dzenNoch) help.setTextColor(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorIcons))
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
        title_toolbar.setText(by.carkva_gazeta.malitounik.R.string.zakladki_bible)
        if (dzenNoch) {
            toolbar.popupTheme = by.carkva_gazeta.malitounik.R.style.AppCompatDark
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(by.carkva_gazeta.malitounik.R.id.trash).isVisible = data.size != 0
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val infl = menuInflater
        infl.inflate(by.carkva_gazeta.malitounik.R.menu.zakladki_i_natatki, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        if (id == by.carkva_gazeta.malitounik.R.id.trash) {
            val natatki = DialogDeliteAllZakladkiINatatki.getInstance(resources.getString(by.carkva_gazeta.malitounik.R.string.zakladki_bible).toLowerCase(Locale.getDefault()), semuxa)
            natatki.show(supportFragmentManager, "delite_all_zakladki_i_natatki")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (semuxa == 1) {
            if (MenuBibleSemuxa.bible_time) {
                MenuBibleSemuxa.bible_time = false
                onSupportNavigateUp()
            } else {
                super.onBackPressed()
            }
        }
        if (semuxa == 2) {
            if (MenuBibleSinoidal.bible_time) {
                MenuBibleSinoidal.bible_time = false
                onSupportNavigateUp()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTollbarTheme()
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
    }

    override fun natatkidiliteItem(position: Int, semuxa: Int) {}

    override fun zakladkadiliteItemCancel() {
        drag_list_view.resetSwipedViews(null)
    }

    override fun zakladkadiliteItem(position: Int, semuxa: Int) {
        if (semuxa == 1) {
            data.removeAt(position)
            adapter.notifyDataSetChanged()
            val fileZakladki = File("$filesDir/BibliaSemuxaZakladki.json")
            if (data.size == 0) {
                if (fileZakladki.exists()) {
                    fileZakladki.delete()
                }
                help.visibility = View.VISIBLE
                drag_list_view.visibility = View.GONE
            } else {
                val gson = Gson()
                fileZakladki.writer().use {
                    it.write(gson.toJson(data))
                }
            }
        }
        if (semuxa == 2) {
            data.removeAt(position)
            adapter.notifyDataSetChanged()
            val fileZakladki = File("$filesDir/BibliaSinodalZakladki.json")
            if (data.size == 0) {
                if (fileZakladki.exists()) {
                    fileZakladki.delete()
                }
                help.visibility = View.VISIBLE
                drag_list_view.visibility = View.GONE
            } else {
                val gson = Gson()
                fileZakladki.writer().use {
                    it.write(gson.toJson(data))
                }
            }
        }
        invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 500) {
            adapter.notifyDataSetChanged()
        }
    }

    private inner class ItemAdapter(list: ArrayList<BibleZakladkiData>, private val mLayoutId: Int, private val mGrabHandleId: Int, private val mDragOnLongPress: Boolean) : DragItemAdapter<BibleZakladkiData, ItemAdapter.ViewHolder>() {
        private var dzenNoch = false
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(mLayoutId, parent, false)
            (view as ListSwipeItem).supportedSwipeDirection = ListSwipeItem.SwipeDirection.LEFT
            val textview = view.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.text)
            val k = parent.context.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            dzenNoch = k.getBoolean("dzen_noch", false)
            textview.textSize = SettingsActivity.GET_FONT_SIZE_MIN
            if (dzenNoch) {
                ExpArrayAdapterParallel.colors[0] = "#FFFFFF"
                ExpArrayAdapterParallel.colors[1] = "#f44336"
                val itemLeft = view.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.item_left)
                itemLeft.setTextColor(ContextCompat.getColor(parent.context, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
                //itemLeft.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
                val itemRight = view.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.item_right)
                itemRight.setTextColor(ContextCompat.getColor(parent.context, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
                //itemRight.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
                view.findViewById<ConstraintLayout>(by.carkva_gazeta.malitounik.R.id.item_layout).setBackgroundResource(by.carkva_gazeta.malitounik.R.drawable.selector_dark)
                //textview.setTextColor(ContextCompat.getColor(parent.context, by.carkva_gazeta.malitounik.R.color.colorIcons))
                view.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
            } else {
                ExpArrayAdapterParallel.colors[0] = "#000000"
                ExpArrayAdapterParallel.colors[1] = "#D00505"
                //textview.setTextColor(ContextCompat.getColor(parent.context, by.carkva_gazeta.malitounik.R.color.colorPrimary_text))
                view.findViewById<ConstraintLayout>(by.carkva_gazeta.malitounik.R.id.item_layout).setBackgroundResource(by.carkva_gazeta.malitounik.R.drawable.selector_default)
                view.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorDivider)
            }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            val text = mItemList[position].data
            val t1 = text.lastIndexOf("<!--")
            val t2 = text.indexOf("\n\n")
            var colorPosition = 0
            val textItem = if (t1 == -1) {
                SpannableString(text)
            } else {
                colorPosition = text.substring(t1 + 4).toInt()
                SpannableString(text.substring(0, t1))
            }
            textItem.setSpan(ForegroundColorSpan(Color.parseColor(ExpArrayAdapterParallel.colors[colorPosition])), 0, t2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.mText.text = textItem
            holder.itemView.tag = mItemList[position]
        }

        override fun getUniqueItemId(position: Int): Long {
            return mItemList[position].id
        }

        private inner class ViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, mDragOnLongPress) {
            var mText: TextView = itemView.findViewById(by.carkva_gazeta.malitounik.R.id.text)
            override fun onItemClicked(view: View) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                val knigaName = mItemList[adapterPosition].data
                var kniga = -1
                var knigaS = -1
                var t1: Int
                var t2 = 0
                var t3 = 0
                var glava = 0
                if (semuxa == 1) {
                    if (knigaName.contains("Паводле Мацьвея")) kniga = 0
                    if (knigaName.contains("Паводле Марка")) kniga = 1
                    if (knigaName.contains("Паводле Лукаша")) kniga = 2
                    if (knigaName.contains("Паводле Яна")) kniga = 3
                    if (knigaName.contains("Дзеі Апосталаў")) kniga = 4
                    if (knigaName.contains("Якава")) kniga = 5
                    if (knigaName.contains("1-е Пятра")) kniga = 6
                    if (knigaName.contains("2-е Пятра")) kniga = 7
                    if (knigaName.contains("1-е Яна Багаслова")) kniga = 8
                    if (knigaName.contains("2-е Яна Багаслова")) kniga = 9
                    if (knigaName.contains("3-е Яна Багаслова")) kniga = 10
                    if (knigaName.contains("Юды")) kniga = 11
                    if (knigaName.contains("Да Рымлянаў")) kniga = 12
                    if (knigaName.contains("1-е да Карынфянаў")) kniga = 13
                    if (knigaName.contains("2-е да Карынфянаў")) kniga = 14
                    if (knigaName.contains("Да Галятаў")) kniga = 15
                    if (knigaName.contains("Да Эфэсянаў")) kniga = 16
                    if (knigaName.contains("Да Піліпянаў")) kniga = 17
                    if (knigaName.contains("Да Каласянаў")) kniga = 18
                    if (knigaName.contains("1-е да Фесаланікійцаў")) kniga = 19
                    if (knigaName.contains("2-е да Фесаланікійцаў")) kniga = 20
                    if (knigaName.contains("1-е да Цімафея")) kniga = 21
                    if (knigaName.contains("2-е да Цімафея")) kniga = 22
                    if (knigaName.contains("Да Ціта")) kniga = 23
                    if (knigaName.contains("Да Філімона")) kniga = 24
                    if (knigaName.contains("Да Габрэяў")) kniga = 25
                    if (knigaName.contains("Адкрыцьцё (Апакаліпсіс)")) kniga = 26
                    if (knigaName.contains("Быцьцё")) knigaS = 0
                    if (knigaName.contains("Выхад")) knigaS = 1
                    if (knigaName.contains("Лявіт")) knigaS = 2
                    if (knigaName.contains("Лікі")) knigaS = 3
                    if (knigaName.contains("Другі Закон")) knigaS = 4
                    if (knigaName.contains("Ісуса сына Нава")) knigaS = 5
                    if (knigaName.contains("Судзьдзяў")) knigaS = 6
                    if (knigaName.contains("Рут")) knigaS = 7
                    if (knigaName.contains("1-я Царстваў")) knigaS = 8
                    if (knigaName.contains("2-я Царстваў")) knigaS = 9
                    if (knigaName.contains("3-я Царстваў")) knigaS = 10
                    if (knigaName.contains("4-я Царстваў")) knigaS = 11
                    if (knigaName.contains("1-я Летапісаў")) knigaS = 12
                    if (knigaName.contains("2-я Летапісаў")) knigaS = 13
                    if (knigaName.contains("Эздры")) knigaS = 14
                    if (knigaName.contains("Нээміі")) knigaS = 15
                    if (knigaName.contains("Эстэр")) knigaS = 16
                    if (knigaName.contains("Ёва")) knigaS = 17
                    if (knigaName.contains("Псалтыр")) knigaS = 18
                    if (knigaName.contains("Выслоўяў Саламонавых")) knigaS = 19
                    if (knigaName.contains("Эклезіяста")) knigaS = 20
                    if (knigaName.contains("Найвышэйшая Песьня Саламонава")) knigaS = 21
                    if (knigaName.contains("Ісаі")) knigaS = 22
                    if (knigaName.contains("Ераміі")) knigaS = 23
                    if (knigaName.contains("Ераміін Плач")) knigaS = 24
                    if (knigaName.contains("Езэкііля")) knigaS = 25
                    if (knigaName.contains("Данііла")) knigaS = 26
                    if (knigaName.contains("Асіі")) knigaS = 27
                    if (knigaName.contains("Ёіля")) knigaS = 28
                    if (knigaName.contains("Амоса")) knigaS = 29
                    if (knigaName.contains("Аўдзея")) knigaS = 30
                    if (knigaName.contains("Ёны")) knigaS = 31
                    if (knigaName.contains("Міхея")) knigaS = 32
                    if (knigaName.contains("Навума")) knigaS = 33
                    if (knigaName.contains("Абакума")) knigaS = 34
                    if (knigaName.contains("Сафона")) knigaS = 35
                    if (knigaName.contains("Агея")) knigaS = 36
                    if (knigaName.contains("Захарыі")) knigaS = 37
                    if (knigaName.contains("Малахіі")) knigaS = 38
                    t1 = knigaName.indexOf("Разьдзел ")
                    t2 = knigaName.indexOf("/", t1)
                    t3 = knigaName.indexOf("\n\n")
                    glava = knigaName.substring(t1 + 9, t2).toInt()
                }
                if (semuxa == 2) {
                    if (knigaName.contains("От Матфея")) kniga = 0
                    if (knigaName.contains("От Марка")) kniga = 1
                    if (knigaName.contains("От Луки")) kniga = 2
                    if (knigaName.contains("От Иоанна")) kniga = 3
                    if (knigaName.contains("Деяния святых апостолов")) kniga = 4
                    if (knigaName.contains("Иакова")) kniga = 5
                    if (knigaName.contains("1-е Петра")) kniga = 6
                    if (knigaName.contains("2-е Петра")) kniga = 7
                    if (knigaName.contains("1-е Иоанна")) kniga = 8
                    if (knigaName.contains("2-е Иоанна")) kniga = 9
                    if (knigaName.contains("3-е Иоанна")) kniga = 10
                    if (knigaName.contains("Иуды")) kniga = 11
                    if (knigaName.contains("Римлянам")) kniga = 12
                    if (knigaName.contains("1-е Коринфянам")) kniga = 13
                    if (knigaName.contains("2-е Коринфянам")) kniga = 14
                    if (knigaName.contains("Галатам")) kniga = 15
                    if (knigaName.contains("Ефесянам")) kniga = 16
                    if (knigaName.contains("Филиппийцам")) kniga = 17
                    if (knigaName.contains("Колоссянам")) kniga = 18
                    if (knigaName.contains("1-е Фессалоникийцам (Солунянам)")) kniga = 19
                    if (knigaName.contains("2-е Фессалоникийцам (Солунянам)")) kniga = 20
                    if (knigaName.contains("1-е Тимофею")) kniga = 21
                    if (knigaName.contains("2-е Тимофею")) kniga = 22
                    if (knigaName.contains("Титу")) kniga = 23
                    if (knigaName.contains("Филимону")) kniga = 24
                    if (knigaName.contains("Евреям")) kniga = 25
                    if (knigaName.contains("Откровение (Апокалипсис)")) kniga = 26
                    if (knigaName.contains("Бытие")) knigaS = 0
                    if (knigaName.contains("Исход")) knigaS = 1
                    if (knigaName.contains("Левит")) knigaS = 2
                    if (knigaName.contains("Числа")) knigaS = 3
                    if (knigaName.contains("Второзаконие")) knigaS = 4
                    if (knigaName.contains("Иисуса Навина")) knigaS = 5
                    if (knigaName.contains("Судей израилевых")) knigaS = 6
                    if (knigaName.contains("Руфи")) knigaS = 7
                    if (knigaName.contains("1-я Царств")) knigaS = 8
                    if (knigaName.contains("2-я Царств")) knigaS = 9
                    if (knigaName.contains("3-я Царств")) knigaS = 10
                    if (knigaName.contains("4-я Царств")) knigaS = 11
                    if (knigaName.contains("1-я Паралипоменон")) knigaS = 12
                    if (knigaName.contains("2-я Паралипоменон")) knigaS = 13
                    if (knigaName.contains("1-я Ездры")) knigaS = 14
                    if (knigaName.contains("Неемии")) knigaS = 15
                    if (knigaName.contains("2-я Ездры")) knigaS = 16
                    if (knigaName.contains("Товита")) knigaS = 17
                    if (knigaName.contains("Иудифи")) knigaS = 18
                    if (knigaName.contains("Есфири")) knigaS = 19
                    if (knigaName.contains("Иова")) knigaS = 20
                    if (knigaName.contains("Псалтирь")) knigaS = 21
                    if (knigaName.contains("Притчи Соломона")) knigaS = 22
                    if (knigaName.contains("Екклезиаста")) knigaS = 23
                    if (knigaName.contains("Песнь песней Соломона")) knigaS = 24
                    if (knigaName.contains("Премудрости Соломона")) knigaS = 25
                    if (knigaName.contains("Премудрости Иисуса, сына Сирахова")) knigaS = 26
                    if (knigaName.contains("Исаии")) knigaS = 27
                    if (knigaName.contains("Иеремии")) knigaS = 28
                    if (knigaName.contains("Плач Иеремии")) knigaS = 29
                    if (knigaName.contains("Послание Иеремии")) knigaS = 30
                    if (knigaName.contains("Варуха")) knigaS = 31
                    if (knigaName.contains("Иезекииля")) knigaS = 32
                    if (knigaName.contains("Даниила")) knigaS = 33
                    if (knigaName.contains("Осии")) knigaS = 34
                    if (knigaName.contains("Иоиля")) knigaS = 35
                    if (knigaName.contains("Амоса")) knigaS = 36
                    if (knigaName.contains("Авдия")) knigaS = 37
                    if (knigaName.contains("Ионы")) knigaS = 38
                    if (knigaName.contains("Михея")) knigaS = 39
                    if (knigaName.contains("Наума")) knigaS = 40
                    if (knigaName.contains("Аввакума")) knigaS = 41
                    if (knigaName.contains("Сафонии")) knigaS = 42
                    if (knigaName.contains("Аггея")) knigaS = 43
                    if (knigaName.contains("Захарии")) knigaS = 44
                    if (knigaName.contains("Малахии")) knigaS = 45
                    if (knigaName.contains("1-я Маккавейская")) knigaS = 46
                    if (knigaName.contains("2-я Маккавейская")) knigaS = 47
                    if (knigaName.contains("3-я Маккавейская")) knigaS = 48
                    if (knigaName.contains("3-я Ездры")) knigaS = 49
                    t1 = knigaName.indexOf("Глава ")
                    t2 = knigaName.indexOf("/", t1)
                    t3 = knigaName.indexOf("\n\n", t2)
                    glava = knigaName.substring(t1 + 6, t2).toInt()
                }
                val stix = knigaName.substring(t2 + 6, t3).toInt()
                var intent = Intent()
                if (kniga != -1) {
                    if (semuxa == 1) {
                        intent = Intent(this@BibleZakladki, NovyZapavietSemuxa::class.java)
                    }
                    if (semuxa == 2) {
                        intent = Intent(this@BibleZakladki, NovyZapavietSinaidal::class.java)
                    }
                    intent.putExtra("kniga", kniga)
                }
                if (knigaS != -1) {
                    if (semuxa == 1) {
                        intent = Intent(this@BibleZakladki, StaryZapavietSemuxa::class.java)
                    }
                    if (semuxa == 2) {
                        intent = Intent(this@BibleZakladki, StaryZapavietSinaidal::class.java)
                    }
                    intent.putExtra("kniga", knigaS)
                }
                intent.putExtra("glava", glava - 1)
                intent.putExtra("stix", stix - 1)
                startActivityForResult(intent, 500)
            }

            override fun onItemLongClicked(view: View): Boolean {
                val t1 = itemList[adapterPosition].data.indexOf("\n\n")
                val t2: Int
                t2 = if (semuxa == 1) itemList[adapterPosition].data.indexOf(". ", t1) else itemList[adapterPosition].data.indexOf(" ", t1)
                val delite = DialogZakladkaDelite.getInstance(adapterPosition, itemList[adapterPosition].data.substring(0, t1) + getString(by.carkva_gazeta.malitounik.R.string.stix_by) + " " + itemList[adapterPosition].data.substring(t1 + 2, t2), semuxa, true)
                delite.show(supportFragmentManager, "zakladka_delite")
                return true
            }
        }

        init {
            itemList = list
        }
    }

    private class MyDragItem(context: Context, layoutId: Int) : DragItem(context, layoutId) {
        //private val mycontext = context
        override fun onBindDragView(clickedView: View, dragView: View) {
            val text = (clickedView.findViewById<View>(by.carkva_gazeta.malitounik.R.id.text) as TextView).text
            val dragTextView = dragView.findViewById<View>(by.carkva_gazeta.malitounik.R.id.text) as TextView
            dragTextView.text = text
            dragTextView.textSize = SettingsActivity.GET_FONT_SIZE_MIN
            /*val k = mycontext.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            val dzenNoch = k.getBoolean("dzen_noch", false)
            if (dzenNoch) {
                clickedView.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.text).setCompoundDrawablesWithIntrinsicBounds(by.carkva_gazeta.malitounik.R.drawable.stiker_black, 0, 0, 0)
                clickedView.findViewById<ConstraintLayout>(by.carkva_gazeta.malitounik.R.id.item_layout).setBackgroundResource(by.carkva_gazeta.malitounik.R.drawable.selector_dark)
                val itemLeft = clickedView.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.item_left)
                itemLeft.setTextColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
                itemLeft.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
                val itemRight = clickedView.findViewById<TextViewRobotoCondensed>(by.carkva_gazeta.malitounik.R.id.item_right)
                itemRight.setTextColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
                itemRight.setBackgroundResource(by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark)
                dragTextView.setTextColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorIcons))
                dragView.findViewById<View>(by.carkva_gazeta.malitounik.R.id.item_layout).setBackgroundColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorprimary_material_dark))
            } else {
                dragTextView.setTextColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorPrimary_text))
                dragView.findViewById<View>(by.carkva_gazeta.malitounik.R.id.item_layout).setBackgroundColor(ContextCompat.getColor(mycontext, by.carkva_gazeta.malitounik.R.color.colorDivider))
            }*/
        }
    }

    /*private inner class BibleZakladkiListAdaprer(private val mContext: Activity, private val itemsL: ArrayList<String>) : ArrayAdapter<String>(mContext, by.carkva_gazeta.malitounik.R.layout.simple_list_item_3, by.carkva_gazeta.malitounik.R.id.label, itemsL) {
        private val k: SharedPreferences = mContext.getSharedPreferences("biblia", Context.MODE_PRIVATE)
        override fun add(string: String?) {
            super.add(string)
            itemsL.add(string ?: "")
        }

        override fun remove(string: String?) {
            super.remove(string)
            itemsL.remove(string)
        }

        override fun clear() {
            super.clear()
            itemsL.clear()
        }

        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolder
            if (mView == null) {
                rootView = mContext.layoutInflater.inflate(by.carkva_gazeta.malitounik.R.layout.simple_list_item_3, parent, false)
                viewHolder = ViewHolder()
                rootView.tag = viewHolder
                viewHolder.text = rootView.findViewById(by.carkva_gazeta.malitounik.R.id.label)
                viewHolder.buttonPopup = rootView.findViewById(by.carkva_gazeta.malitounik.R.id.button_popup)
            } else {
                rootView = mView
                viewHolder = rootView.tag as ViewHolder
            }
            val dzenNoch = k.getBoolean("dzen_noch", false)
            if (dzenNoch) {
                viewHolder.text?.setBackgroundResource(by.carkva_gazeta.malitounik.R.drawable.selector_dark)
                viewHolder.text?.setTextColor(ContextCompat.getColor(mContext, by.carkva_gazeta.malitounik.R.color.colorIcons))
                viewHolder.text?.setCompoundDrawablesWithIntrinsicBounds(by.carkva_gazeta.malitounik.R.drawable.stiker_black, 0, 0, 0)
                ExpArrayAdapterParallel.colors[0] = "#FFFFFF"
                ExpArrayAdapterParallel.colors[1] = "#f44336"
            } else {
                viewHolder.text?.setBackgroundResource(by.carkva_gazeta.malitounik.R.drawable.selector_white)
                ExpArrayAdapterParallel.colors[0] = "#000000"
                ExpArrayAdapterParallel.colors[1] = "#D00505"
            }
            viewHolder.buttonPopup?.setOnClickListener { viewHolder.buttonPopup?.let { showPopupMenu(it, position, itemsL[position]) } }
            val t1 = itemsL[position].lastIndexOf("<!--")
            val t2 = itemsL[position].indexOf("\n\n")
            var colorPosition = 0
            val textItem = if (t1 == -1) {
                SpannableString(itemsL[position])
            } else {
                colorPosition = itemsL[position].substring(t1 + 4).toInt()
                SpannableString(itemsL[position].substring(0, t1))
            }
            textItem.setSpan(ForegroundColorSpan(Color.parseColor(ExpArrayAdapterParallel.colors[colorPosition])), 0, t2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            viewHolder.text?.text = textItem
            viewHolder.text?.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            return rootView
        }

        private fun showPopupMenu(view: View, position: Int, name: String) {
            val popup = PopupMenu(mContext, view)
            val infl = popup.menuInflater
            infl.inflate(by.carkva_gazeta.malitounik.R.menu.popup, popup.menu)
            popup.menu.getItem(0).isVisible = false
            for (i in 0 until popup.menu.size()) {
                val item = popup.menu.getItem(i)
                val spanString = SpannableString(popup.menu.getItem(i).title.toString())
                val end = spanString.length
                spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                item.title = spanString
            }
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                popup.dismiss()
                when (menuItem.itemId) {
                    by.carkva_gazeta.malitounik.R.id.menu_redoktor -> return@setOnMenuItemClickListener true
                    by.carkva_gazeta.malitounik.R.id.menu_remove -> {
                        val t1 = name.indexOf("\n\n")
                        val t2: Int
                        t2 = if (semuxa == 1) name.indexOf(". ", t1) else name.indexOf(" ", t1)
                        val delite = DialogZakladkaDelite.getInstance(position, name.substring(0, t1) + getString(by.carkva_gazeta.malitounik.R.string.stix_by) + " " + name.substring(t1 + 2, t2), semuxa, true)
                        delite.show(supportFragmentManager, "zakladka_delite")
                        return@setOnMenuItemClickListener true
                    }
                }
                false
            }
            popup.show()
        }
    }

    private class ViewHolder {
        var text: TextViewRobotoCondensed? = null
        var buttonPopup: ImageView? = null
    }*/
}