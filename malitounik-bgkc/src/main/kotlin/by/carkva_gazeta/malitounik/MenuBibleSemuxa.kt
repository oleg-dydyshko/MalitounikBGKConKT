package by.carkva_gazeta.malitounik

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.menu_bible.*

class MenuBibleSemuxa : Fragment() {
    private var mLastClickTime: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.menu_bible, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { activity ->
            val k = activity.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            val dzenNoch = k.getBoolean("dzen_noch", false)
            val bibleVybranoe = k.getString("bibleVybranoeSemuxa", "") ?: ""
            if (bibleVybranoe == "") {
                myBible.visibility = View.GONE
            } else {
                val gson = Gson()
                val type = object : TypeToken<ArrayList<VybranoeBibliaData>>() {}.type
                val arrayListVybranoe: ArrayList<VybranoeBibliaData> = gson.fromJson(bibleVybranoe, type)
                if (arrayListVybranoe.isEmpty()) myBible.visibility = View.GONE
            }
            myBible.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                MyBibleList.biblia = 1
                startActivity(Intent(activity, MyBibleList::class.java))
            }
            novyZavet.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                startActivity(Intent(activity, NovyZapavietSemuxaList::class.java))
            }
            staryZavet.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                startActivity(Intent(activity, StaryZapavietSemuxaList::class.java))
            }
            val bibleTime = k.getString("bible_time_semuxa", "") ?: ""
            if (bibleTime == "") {
                bible_time = true
                prodolzych.visibility = View.GONE
            }
            prodolzych.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                val gson = Gson()
                val type = object : TypeToken<ArrayMap<String?, Int?>?>() {}.type
                val set: ArrayMap<String, Int> = gson.fromJson(bibleTime, type)
                if (set["zavet"] == 1) {
                    if (MainActivity.checkmoduleResources(activity)) {
                        val intent = Intent(activity, NovyZapavietSemuxaList::class.java)
                        intent.putExtra("kniga", set["kniga"])
                        intent.putExtra("glava", set["glava"])
                        intent.putExtra("stix", set["stix"])
                        intent.putExtra("prodolzyt", true)
                        startActivity(intent)
                    } else {
                        val dadatak = DialogInstallDadatak()
                        fragmentManager?.let { dadatak.show(it, "dadatak") }
                    }
                } else {
                    if (MainActivity.checkmoduleResources(activity)) {
                        val intent = Intent(activity, StaryZapavietSemuxaList::class.java)
                        intent.putExtra("kniga", set["kniga"])
                        intent.putExtra("glava", set["glava"])
                        intent.putExtra("stix", set["stix"])
                        intent.putExtra("prodolzyt", true)
                        startActivity(intent)
                    } else {
                        val dadatak = DialogInstallDadatak()
                        fragmentManager?.let { dadatak.show(it, "dadatak") }
                    }
                }
            }
            zakladki.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                if (MainActivity.checkmoduleResources(activity)) {
                    val intent = Intent(activity, Class.forName("by.carkva_gazeta.resources.BibleZakladki"))
                    intent.putExtra("semuxa", 1)
                    startActivity(intent)
                } else {
                    val dadatak = DialogInstallDadatak()
                    fragmentManager?.let { dadatak.show(it, "dadatak") }
                }
            }
            natatki.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                if (MainActivity.checkmoduleResources(activity)) {
                    val intent = Intent(activity, Class.forName("by.carkva_gazeta.resources.BibleNatatki"))
                    intent.putExtra("semuxa", 1)
                    startActivity(intent)
                } else {
                    val dadatak = DialogInstallDadatak()
                    fragmentManager?.let { dadatak.show(it, "dadatak") }
                }
            }
            saeche.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                if (MainActivity.checkmoduleResources(activity)) {
                    val intent = Intent(activity, Class.forName("by.carkva_gazeta.resources.SearchBiblia"))
                    intent.putExtra("zavet", 1)
                    startActivity(intent)
                } else {
                    val dadatak = DialogInstallDadatak()
                    fragmentManager?.let { dadatak.show(it, "dadatak") }
                }
            }
            umovy_karystannia.setOnClickListener {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return@setOnClickListener
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                val semukha = DialogAlesyaSemukha()
                fragmentManager?.let { semukha.show(it, "Alesya_Semukha") }
            }
            if (dzenNoch) {
                novyZavet.setBackgroundResource(R.drawable.knopka_red_black)
                staryZavet.setBackgroundResource(R.drawable.knopka_red_black)
            }
        }
    }

    companion object {
        var bible_time = false
    }
}