package by.carkva_gazeta.malitounik

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.databinding.DialogTextviewDisplayBinding

class DialogDeliteBibliaVybranoe : DialogFragment() {
    private var mListener: DialogDeliteBibliVybranoeListener? = null
    private var position = 0
    private lateinit var title: String
    private lateinit var alert: AlertDialog
    private var _binding: DialogTextviewDisplayBinding? = null
    private val binding get() = _binding!!

    interface DialogDeliteBibliVybranoeListener {
        fun vybranoeDelite(position: Int)
        fun vybranoeDeliteCancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mListener?.vybranoeDeliteCancel()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position") ?: 0
        title = arguments?.getString("title") ?: ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogDeliteBibliVybranoeListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$activity must implement DialogDeliteBibliVybranoeListener")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            _binding = DialogTextviewDisplayBinding.inflate(LayoutInflater.from(it))
            val k = it.getSharedPreferences("biblia", Context.MODE_PRIVATE)
            val dzenNoch = k.getBoolean("dzen_noch", false)
            var style = R.style.AlertDialogTheme
            if (dzenNoch) style = R.style.AlertDialogThemeBlack
            val builder = AlertDialog.Builder(it, style)
            if (dzenNoch) binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary_black))
            else binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            binding.title.text = resources.getString(R.string.remove)
            binding.content.text = getString(R.string.vybranoe_biblia_delite, title)
            binding.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            if (dzenNoch) binding.content.setTextColor(ContextCompat.getColor(it, R.color.colorWhite)) 
            else binding.content.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            builder.setPositiveButton(resources.getText(R.string.ok)) { _: DialogInterface?, _: Int -> mListener?.vybranoeDelite(position) }
            builder.setNegativeButton(resources.getString(R.string.cansel)) { _: DialogInterface, _: Int -> mListener?.vybranoeDeliteCancel() }
            builder.setView(binding.root)
            alert = builder.create()
        }
        return alert
    }

    companion object {
        fun getInstance(position: Int, title: String): DialogDeliteBibliaVybranoe {
            val dialogDelite = DialogDeliteBibliaVybranoe()
            val bundle = Bundle()
            bundle.putInt("position", position)
            bundle.putString("title", title)
            dialogDelite.arguments = bundle
            return dialogDelite
        }
    }
}