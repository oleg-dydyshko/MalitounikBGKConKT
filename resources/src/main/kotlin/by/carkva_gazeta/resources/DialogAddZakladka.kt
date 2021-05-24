package by.carkva_gazeta.resources

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.R
import by.carkva_gazeta.malitounik.databinding.DialogSpinnerDisplayBinding
import by.carkva_gazeta.malitounik.databinding.SimpleListItemColorBinding

class DialogAddZakladka : DialogFragment() {
    private var dzenNoch = false
    private lateinit var alert: AlertDialog
    private var dialogAddZakladkiListiner: DialogAddZakladkiListiner? = null
    private var color = 0
    private var _binding: DialogSpinnerDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface DialogAddZakladkiListiner {
        fun addZakladka(color: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity)
            dialogAddZakladkiListiner = try {
                context as DialogAddZakladkiListiner
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogAddZakladkiListiner")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogAddZakladkiListiner?.addZakladka(-1)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            _binding = DialogSpinnerDisplayBinding.inflate(LayoutInflater.from(it))
            val k = it.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            dzenNoch = k.getBoolean("dzen_noch", false)
            var style = R.style.AlertDialogTheme
            if (dzenNoch) style = R.style.AlertDialogThemeBlack
            val builder = AlertDialog.Builder(it, style)
            if (dzenNoch) {
                BibleArrayAdapterParallel.colors[0] = "#FFFFFF"
                BibleArrayAdapterParallel.colors[1] = "#f44336"
                binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary_black))
            } else {
                BibleArrayAdapterParallel.colors[0] = "#000000"
                BibleArrayAdapterParallel.colors[1] = "#D00505"
                binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            }
            binding.title.text = resources.getString(R.string.add_color_zakladka)
            binding.content.adapter = ColorAdapter(it)
            binding.content.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    color = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            builder.setView(binding.root)
            builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                dialogAddZakladkiListiner?.addZakladka(color)
            }
            builder.setNegativeButton(getString(R.string.cansel)) { _: DialogInterface?, _: Int ->
                dialogAddZakladkiListiner?.addZakladka(-1)
            }
            alert = builder.create()
        }
        return alert
    }

    private inner class ColorAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.simple_list_item_color, R.id.label, BibleArrayAdapterParallel.colors) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolderColor
            if (convertView == null) {
                val binding = SimpleListItemColorBinding.inflate(LayoutInflater.from(context), parent, false)
                rootView = binding.root
                viewHolder = ViewHolderColor(binding.label)
                rootView.tag = viewHolder
            } else {
                rootView = convertView
                viewHolder = rootView.tag as ViewHolderColor
            }
            viewHolder.text.setBackgroundColor(Color.parseColor(BibleArrayAdapterParallel.colors[position]))
            return rootView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val text = view.findViewById<TextView>(R.id.label)
            text.text = ""
            text.setBackgroundColor(Color.parseColor(BibleArrayAdapterParallel.colors[position]))
            return view
        }
    }

    private class ViewHolderColor(var text: TextView)
}