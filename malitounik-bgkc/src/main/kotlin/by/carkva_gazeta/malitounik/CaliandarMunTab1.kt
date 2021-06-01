package by.carkva_gazeta.malitounik

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import by.carkva_gazeta.malitounik.databinding.CalendatTab1Binding
import by.carkva_gazeta.malitounik.databinding.SimpleListItem4Binding
import java.util.*

class CaliandarMunTab1 : Fragment() {
    private lateinit var adapterViewPager: SmartFragmentStatePagerAdapter
    private var dzenNoch = false
    private val names = arrayOf("СТУДЗЕНЬ", "ЛЮТЫ", "САКАВІК", "КРАСАВІК", "ТРАВЕНЬ", "ЧЭРВЕНЬ", "ЛІПЕНЬ", "ЖНІВЕНЬ", "ВЕРАСЕНЬ", "КАСТРЫЧНІК", "ЛІСТАПАД", "СЬНЕЖАНЬ")
    private var day = 0
    private var posMun = 0
    private var yearG = 0
    private var _binding: CalendatTab1Binding? = null
    private val binding get() = _binding!!
    private var munListener: CaliandarMunTab1Listener? = null

    interface CaliandarMunTab1Listener {
        fun setDayAndMun1(day: Int, mun: Int, year: Int)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            munListener = try {
                context as CaliandarMunTab1Listener
            } catch (e: ClassCastException) {
                throw ClassCastException("$activity must implement CaliandarMunTab1Listener")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CalendatTab1Binding.inflate(inflater, container, false)
        activity?.let { activity ->
            val chin = activity.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            dzenNoch = chin.getBoolean("dzen_noch", false)
            day = arguments?.getInt("day") ?: 0
            posMun = arguments?.getInt("posMun") ?: 0
            yearG = arguments?.getInt("yearG") ?: 0
            val adapter = CaliandarMunAdapter(activity, names)
            binding.spinner.adapter = adapter
            val data2 = ArrayList<String>()
            for (i in SettingsActivity.GET_CALIANDAR_YEAR_MIN..SettingsActivity.GET_CALIANDAR_YEAR_MAX) {
                data2.add(i.toString())
            }
            val adapter2 = CaliandarMunAdapter(activity, data2)
            binding.spinner2.adapter = adapter2

            adapterViewPager = MyPagerAdapter(childFragmentManager)
            binding.pager.adapter = adapterViewPager
            val c = Calendar.getInstance() as GregorianCalendar
            val son = (yearG - SettingsActivity.GET_CALIANDAR_YEAR_MIN) * 12 + posMun
            binding.pager.currentItem = son
            binding.spinner.setSelection(posMun)
            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val son1 = (yearG - SettingsActivity.GET_CALIANDAR_YEAR_MIN) * 12 + position
                    posMun = position
                    val pagepos1 = binding.pager.currentItem
                    if (pagepos1 != son1) {
                        binding.pager.currentItem = son1
                    }
                    munListener?.setDayAndMun1(day, posMun, yearG)
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
            binding.spinner2.setSelection(yearG - SettingsActivity.GET_CALIANDAR_YEAR_MIN)
            binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    yearG = (parent.selectedItem as String).toInt()
                    val son1 = (yearG - SettingsActivity.GET_CALIANDAR_YEAR_MIN) * 12 + posMun
                    val pagepos1 = binding.pager.currentItem
                    if (pagepos1 != son1) {
                        binding.pager.currentItem = son1
                        (binding.spinner.adapter as CaliandarMunAdapter).notifyDataSetChanged()
                    }
                    munListener?.setDayAndMun1(day, posMun, yearG)
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {}
            }
            binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    for (i in 0 until adapterViewPager.count) {
                        if (position == i) {
                            var r = SettingsActivity.GET_CALIANDAR_YEAR_MIN
                            var t = 0
                            for (s in 0..c[Calendar.YEAR] - SettingsActivity.GET_CALIANDAR_YEAR_MIN + 2) {
                                for (w in 0..11) {
                                    if (i == t) {
                                        yearG = r
                                        posMun = w

                                    }
                                    t++
                                }
                                r++
                            }
                            binding.spinner.setSelection(posMun)
                            binding.spinner2.setSelection(yearG - SettingsActivity.GET_CALIANDAR_YEAR_MIN)
                            munListener?.setDayAndMun1(day, posMun, yearG)
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_padzeia) {
            activity?.let {
                CaliandarMun.SabytieOnView = !CaliandarMun.SabytieOnView
                val messege: String = if (!CaliandarMun.SabytieOnView) {
                    resources.getString(R.string.sabytie_disable_mun)
                } else {
                    resources.getString(R.string.sabytie_enable_mun)
                }
                MainActivity.toastView(messege)
                adapterViewPager.notifyDataSetChanged()
                it.invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class CaliandarMunAdapter : ArrayAdapter<String> {
        private var arrayList: List<String>? = null

        constructor(context: Context, strings: Array<String>) : super(context, R.layout.simple_list_item_4, strings)
        constructor(context: Context, list: List<String>) : super(context, R.layout.simple_list_item_4, list) {
            arrayList = list
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val day = Calendar.getInstance() as GregorianCalendar
            val v = super.getDropDownView(position, convertView, parent)
            val textView = v as TextView
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_DEFAULT_FONT_SIZE)
            if (dzenNoch) textView.setBackgroundResource(R.drawable.selector_dark)
            else textView.setBackgroundResource(R.drawable.selector_default)
            if (arrayList == null) {
                if (day[Calendar.MONTH] == position) {
                    textView.typeface = MainActivity.createFont(Typeface.BOLD)
                } else {
                    textView.typeface = MainActivity.createFont(Typeface.NORMAL)
                }
            } else {
                if (day[Calendar.YEAR] == position + SettingsActivity.GET_CALIANDAR_YEAR_MIN) {
                    textView.typeface = MainActivity.createFont(Typeface.BOLD)
                } else {
                    textView.typeface = MainActivity.createFont(Typeface.NORMAL)
                }
            }
            return v
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val convert: View
            val viewHolder: ViewHolder
            val day = Calendar.getInstance() as GregorianCalendar
            if (convertView == null) {
                val binding = SimpleListItem4Binding.inflate(LayoutInflater.from(context), parent, false)
                convert = binding.root
                viewHolder = ViewHolder(binding.text1)
                convert.tag = viewHolder
            } else {
                convert = convertView
                viewHolder = convert.tag as ViewHolder
            }
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_DEFAULT_FONT_SIZE)
            if (dzenNoch) viewHolder.text.setBackgroundResource(R.drawable.selector_dark)
            else viewHolder.text.setBackgroundResource(R.drawable.selector_default)
            if (arrayList == null) {
                if (day[Calendar.MONTH] == position && day[Calendar.YEAR] == binding.spinner2.selectedItemPosition + SettingsActivity.GET_CALIANDAR_YEAR_MIN) {
                    viewHolder.text.typeface = MainActivity.createFont(Typeface.BOLD)
                } else {
                    viewHolder.text.typeface = MainActivity.createFont(Typeface.NORMAL)
                }
                viewHolder.text.text = names[position]
            } else {
                if (day[Calendar.YEAR] == position + SettingsActivity.GET_CALIANDAR_YEAR_MIN) {
                    viewHolder.text.typeface = MainActivity.createFont(Typeface.BOLD)
                } else {
                    viewHolder.text.typeface = MainActivity.createFont(Typeface.NORMAL)
                }
                arrayList?.let { viewHolder.text.text = it[position] }
            }
            return convert
        }
    }

    private inner class MyPagerAdapter(fragmentManager: FragmentManager) : SmartFragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return (SettingsActivity.GET_CALIANDAR_YEAR_MAX - SettingsActivity.GET_CALIANDAR_YEAR_MIN + 1) * 12
        }

        override fun getItem(position: Int): Fragment {
            val g = GregorianCalendar(SettingsActivity.GET_CALIANDAR_YEAR_MIN, 0, day)
            for (i in 0 until count) {
                if (position == i) {
                    return PageFragmentMonth.newInstance(day, g[Calendar.MONTH], g[Calendar.YEAR])
                }
                g.add(Calendar.MONTH, 1)
            }
            return PageFragmentMonth.newInstance(g[Calendar.DATE], g[Calendar.MONTH], g[Calendar.YEAR])
        }
    }

    private class ViewHolder(var text: TextView)

    companion object {
        fun getInstance(posMun: Int, yearG: Int, day: Int): CaliandarMunTab1 {
            val frag = CaliandarMunTab1()
            val bundle = Bundle()
            bundle.putInt("posMun", posMun)
            bundle.putInt("yearG", yearG)
            bundle.putInt("day", day)
            frag.arguments = bundle
            return frag
        }
    }
}