package by.carkva_gazeta.biblijateka

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.R
import by.carkva_gazeta.malitounik.SettingsActivity
import by.carkva_gazeta.malitounik.databinding.DialogListviewDisplayBinding
import by.carkva_gazeta.malitounik.databinding.SimpleListItem2Binding

class DialogTitleBiblioteka : DialogFragment() {
    private var bookmarks: ArrayList<String> = ArrayList()
    private var mListener: DialogTitleBibliotekaListener? = null
    private lateinit var chin: SharedPreferences
    private lateinit var alert: AlertDialog
    private var _binding: DialogListviewDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal interface DialogTitleBibliotekaListener {
        fun onDialogTitle(page: Int)
        fun onDialogTitleString(page: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogTitleBibliotekaListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogTitleBibliotekaListener")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookmarks = arguments?.getStringArrayList("bookmarks")?: ArrayList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            _binding = DialogListviewDisplayBinding.inflate(LayoutInflater.from(it))
            chin = it.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            val dzenNoch = chin.getBoolean("dzen_noch", false)
            var style = R.style.AlertDialogTheme
            if (dzenNoch) style = R.style.AlertDialogThemeBlack
            val builder = AlertDialog.Builder(it, style)
            if (dzenNoch) binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary_black))
            else binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            binding.title.text = resources.getString(R.string.zmest).uppercase()
            if (dzenNoch)
                binding.content.selector = ContextCompat.getDrawable(it, R.drawable.selector_dark)
            else
                binding.content.selector = ContextCompat.getDrawable(it, R.drawable.selector_default)
            binding.content.adapter = TitleListAdaprer(it)
            builder.setView(binding.root)
            builder.setPositiveButton(getString(R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alert = builder.create()
            binding.content.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val t1 = bookmarks[i].indexOf("<>")
                if (t1 != -1) {
                    val t2 = bookmarks[i].substring(0, t1).toInt()
                    mListener?.onDialogTitle(t2)
                } else {
                    val t2 = bookmarks[i].indexOf("<str>")
                    mListener?.onDialogTitleString(bookmarks[i].substring(0, t2))
                }
                alert.cancel()
            }
        }
        return alert
    }

    internal inner class TitleListAdaprer(mContext: Activity) : ArrayAdapter<String>(mContext, R.layout.simple_list_item_2, R.id.label, bookmarks) {
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val binding = SimpleListItem2Binding.inflate(LayoutInflater.from(context), parent, false)
                rootView = binding.root
                viewHolder = ViewHolder(binding.label)
                rootView.tag = viewHolder
            } else {
                rootView = mView
                viewHolder = rootView.tag as ViewHolder
            }
            val dzenNoch = chin.getBoolean("dzen_noch", false)
            var t1 = bookmarks[position].indexOf("<>")
            if (t1 == -1) {
                t1 = bookmarks[position].indexOf("<str>")
                viewHolder.text.text = bookmarks[position].substring(t1 + 5)
            } else {
                viewHolder.text.text = bookmarks[position].substring(t1 + 2)
            }
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            if (dzenNoch)
                viewHolder.text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stiker_black, 0, 0, 0)
            return rootView
        }

    }

    private class ViewHolder(var text: TextView)

    companion object {
        fun getInstance(bookmarks: ArrayList<String>?): DialogTitleBiblioteka {
            val instance = DialogTitleBiblioteka()
            val args = Bundle()
            args.putStringArrayList("bookmarks", bookmarks)
            instance.arguments = args
            return instance
        }
    }
}