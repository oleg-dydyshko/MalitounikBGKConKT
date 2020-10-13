package by.carkva_gazeta.resources

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.SettingsActivity
import by.carkva_gazeta.malitounik.TextViewRobotoCondensed

class DialogAddZakladka : DialogFragment() {
    private var realpadding = 0
    private var dzenNoch = false
    private lateinit var alert: AlertDialog
    private var dialogAddZakladkiListiner: DialogAddZakladkiListiner? = null
    private var color = 0

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
            val k = it.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            dzenNoch = k.getBoolean("dzen_noch", false)
            val builder = AlertDialog.Builder(it)
            val linear = LinearLayout(it)
            linear.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextViewRobotoCondensed(it)
            if (dzenNoch) {
                ExpArrayAdapterParallel.colors[0] = "#FFFFFF"
                ExpArrayAdapterParallel.colors[1] = "#f44336"
                textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary_black))
            } else {
                ExpArrayAdapterParallel.colors[0] = "#000000"
                ExpArrayAdapterParallel.colors[1] = "#D00505"
                textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary))
            }
            val density = resources.displayMetrics.density
            realpadding = (10 * density).toInt()
            textViewZaglavie.setPadding(realpadding, realpadding, realpadding, realpadding)
            textViewZaglavie.text = resources.getString(by.carkva_gazeta.malitounik.R.string.add_color_zakladka)
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            textViewZaglavie.setTypeface(null, Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorIcons))
            linear.addView(textViewZaglavie)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(realpadding, realpadding, 0, 0)
            val spinner = Spinner(it)
            spinner.adapter = ColorAdapter(it)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    color = position
                    //spinner.setSelection(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            linear.addView(spinner, layoutParams)
            builder.setView(linear)
            builder.setPositiveButton(getString(by.carkva_gazeta.malitounik.R.string.ok)) { _: DialogInterface?, _: Int ->
                dialogAddZakladkiListiner?.addZakladka(color)
            }
            builder.setNegativeButton(getString(by.carkva_gazeta.malitounik.R.string.CANCEL)) { _: DialogInterface?, _: Int ->
                dialogAddZakladkiListiner?.addZakladka(-1)
            }
            alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_TOAST)
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_TOAST)
            }
        }
        return alert
    }

    private inner class ColorAdapter(context: Context) : ArrayAdapter<String>(context, by.carkva_gazeta.malitounik.R.layout.simple_list_item_color, by.carkva_gazeta.malitounik.R.id.label, ExpArrayAdapterParallel.colors) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolderColor
            if (convertView == null) {
                rootView = layoutInflater.inflate(by.carkva_gazeta.malitounik.R.layout.simple_list_item_color, parent, false)
                viewHolder = ViewHolderColor()
                rootView.tag = viewHolder
                viewHolder.text = rootView.findViewById(by.carkva_gazeta.malitounik.R.id.label)
            } else {
                rootView = convertView
                viewHolder = rootView.tag as ViewHolderColor
            }
            viewHolder.text?.setBackgroundColor(Color.parseColor(ExpArrayAdapterParallel.colors[position]))
            return rootView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val text: TextView = view.findViewById(by.carkva_gazeta.malitounik.R.id.label)
            text.text = ""
            text.setBackgroundColor(Color.parseColor(ExpArrayAdapterParallel.colors[position]))
            return view
        }
    }

    private class ViewHolderColor {
        var text: TextViewRobotoCondensed? = null
    }
}