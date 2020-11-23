package by.carkva_gazeta.malitounik

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ListView
import androidx.fragment.app.ListFragment

class MenuRuzanec : ListFragment() {
    private var mLastClickTime: Long = 0
    private val data: Array<out String>
        get() = resources.getStringArray(R.array.ruzanec)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            val adapter = MenuListAdaprer(it, data)
            listAdapter = adapter
            listView.isVerticalScrollBarEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        if (MainActivity.checkmoduleResources(activity)) {
            activity?.let {
                val intent = Intent()
                intent.setClassName(it, MainActivity.BOGASHLUGBOVYA)
                when (position) {
                    0 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec0")
                    }
                    1 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec2")
                    }
                    2 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec1")
                    }
                    3 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec3")
                    }
                    4 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec4")
                    }
                    5 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec5")
                    }
                    6 -> {
                        intent.putExtra("title", data[position])
                        intent.putExtra("resurs", "ruzanec6")
                    }
                }
                startActivity(intent)
            }
        } else {
            val dadatak = DialogInstallDadatak()
            fragmentManager?.let { dadatak.show(it, "dadatak") }
        }
    }
}