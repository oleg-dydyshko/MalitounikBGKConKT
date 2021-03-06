package by.carkva_gazeta.biblijateka

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.malitounik.*
import by.carkva_gazeta.malitounik.R
import by.carkva_gazeta.malitounik.databinding.DialogTextviewDisplayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class DialogBibliateka : DialogFragment() {
    private var listPosition: String = "0"
    private var title: String = "0"
    private var listStr: String = "0"
    private var size: String = "0"
    private var mListener: DialogBibliatekaListener? = null
    private lateinit var builder: AlertDialog.Builder
    private var _binding: DialogTextviewDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal interface DialogBibliatekaListener {
        fun onDialogbibliatekaPositiveClick(listPosition: String, title: String) //void onDialogbibliatekaNeutralClick(String listPosition, String title);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listPosition = arguments?.getString("listPosition") ?: "0"
        listStr = arguments?.getString("listStr") ?: "0"
        title = arguments?.getString("title") ?: "0"
        size = arguments?.getString("size") ?: "0"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogBibliatekaListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogBibliatekaListener")
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
            builder = AlertDialog.Builder(it, style)
            if (dzenNoch) binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary_black))
            else binding.title.setBackgroundColor(ContextCompat.getColor(it, R.color.colorPrimary))
            val file = File(it.filesDir.toString() + "/Biblijateka/" + listPosition)
            if (file.exists()) {
                binding.title.text = getString(R.string.opisanie).uppercase()
            } else {
                binding.title.text = getString(R.string.download_file, "")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val format = withContext(Dispatchers.IO) {
                            val storageManager = it.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                            val bates = storageManager.getAllocatableBytes(storageManager.getUuidForPath(it.filesDir))
                            val bat = (bates.toFloat() / 1024).toDouble()
                            return@withContext when {
                                bat < 10000f -> getString(R.string.dastupna_bat, formatFigureTwoPlaces(BigDecimal(bat).setScale(2, RoundingMode.HALF_EVEN).toFloat()))
                                bates < 1000L -> getString(R.string.dastupna_bates, bates)
                                else -> ""
                            }
                        }
                        binding.title.text = getString(R.string.download_file, format)
                    }
                }
            }
            binding.content.text = MainActivity.fromHtml(listStr)
            if (dzenNoch) binding.content.setTextColor(ContextCompat.getColor(it, R.color.colorWhite)) 
            else binding.content.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary_text))
            val dirCount = size.toInt()
            val izm = if (dirCount / 1024 > 1000) {
                formatFigureTwoPlaces(BigDecimal.valueOf(dirCount.toFloat() / 1024 / 1024.toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()) + " Мб"
            } else {
                formatFigureTwoPlaces(BigDecimal.valueOf(dirCount.toFloat() / 1024.toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()) + " Кб"
            }
            if (file.exists()) {
                builder.setPositiveButton(resources.getString(R.string.ok)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            } else {
                if (MainActivity.isIntNetworkAvailable() != 0) {
                    builder.setPositiveButton("Спампаваць $izm") { dialog: DialogInterface, _: Int ->
                        mListener?.onDialogbibliatekaPositiveClick(listPosition, title)
                        dialog.cancel()
                    }
                    builder.setNegativeButton(R.string.cansel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                } else {
                    builder.setPositiveButton("НЯМА ІНТЭРНЭТ-ЗЛУЧЭНЬНЯ") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                }
            }
        }
        builder.setView(binding.root)
        return builder.create()
    }

    private fun formatFigureTwoPlaces(value: Float): String {
        val myFormatter = DecimalFormat("##0.00")
        return myFormatter.format(value.toDouble())
    }

    companion object {
        fun getInstance(listPosition: String?, listStr: String?, title: String?, size: String?): DialogBibliateka {
            val instance = DialogBibliateka()
            val args = Bundle()
            args.putString("listPosition", listPosition)
            args.putString("listStr", listStr)
            args.putString("title", title)
            args.putString("size", size)
            instance.arguments = args
            return instance
        }
    }
}