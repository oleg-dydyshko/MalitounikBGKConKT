package by.carkva_gazeta.admin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import by.carkva_gazeta.admin.databinding.AdminSimpleListItemBinding
import by.carkva_gazeta.malitounik.SettingsActivity
import by.carkva_gazeta.malitounik.databinding.DialogListviewDisplayBinding
import java.io.File
import java.io.FilenameFilter

class DialogFileExplorer : DialogFragment() {
    private val str = ArrayList<String>()
    private var firstLvl = true
    private val fileList = ArrayList<MyFile>()
    private var path: File? = null
    private var chosenFile = ""
    private var mListener: DialogFileExplorerListener? = null
    private var sdCard = true
    private var sdCard2 = false
    private lateinit var alert: AlertDialog
    private var _binding: DialogListviewDisplayBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    internal interface DialogFileExplorerListener {
        fun onDialogFile(file: File)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogFileExplorerListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogFileExplorerListener")
            }
        }
    }

    private fun loadFileList() {
        fileList.clear()
        if (path?.exists() == true) {
            val filterDir = FilenameFilter { dir: File, filename: String ->
                val sel = File(dir, filename)
                sel.isDirectory && !sel.isHidden
            }
            val filterFile = FilenameFilter { dir: File, filename: String ->
                val sel = File(dir, filename)
                sel.isFile && !sel.isHidden && (sel.name.contains(".txt", true) || sel.name.contains(".htm", true))
            }
            if (!firstLvl) {
                fileList.add(MyFile("Верх", R.drawable.directory_up))
            } else {
                sdCard2 = true
                sdCard = false
                fileList.add(MyFile("Верх", R.drawable.directory_up))
            }
            val dList = path?.list(filterDir) ?: Array(0) { "" }
            dList.sort()
            for (aFList in dList) {
                fileList.add(MyFile(aFList, R.drawable.directory_icon))
            }
            val fList = path?.list(filterFile) ?: Array(0) { "" }
            dList.sort()
            for (aFList in fList) {
                when {
                    aFList.contains(".htm", true) -> {
                        fileList.add(MyFile(aFList, R.drawable.file_html_icon))
                    }
                    aFList.contains(".txt", true) -> {
                        fileList.add(MyFile(aFList, R.drawable.file_txt_icon))
                    }
                    aFList.contains(".png", true) -> {
                        fileList.add(MyFile(aFList, R.drawable.image_icon_file))
                    }
                    aFList.contains(".jp", true) -> {
                        fileList.add(MyFile(aFList, R.drawable.image_icon_file))
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            _binding = DialogListviewDisplayBinding.inflate(LayoutInflater.from(it))
            val files = ContextCompat.getExternalFilesDirs(it, null)
            fileList.add(MyFile("Унутраная памяць", R.drawable.directory_icon))
            if (files.size > 1) {
                fileList.add(MyFile("Карта SD", R.drawable.directory_icon))
            }
            val builder = AlertDialog.Builder(it, by.carkva_gazeta.malitounik.R.style.AlertDialogTheme)
            binding.title.text = "ВЫБЕРЫЦЕ ФАЙЛ"
            binding.content.selector = ContextCompat.getDrawable(it, by.carkva_gazeta.malitounik.R.drawable.selector_default)
            binding.content.adapter = TitleListAdaprer(it)
            builder.setView(binding.root)
            builder.setPositiveButton(getString(by.carkva_gazeta.malitounik.R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alert = builder.create()
            binding.content.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                if (sdCard) {
                    var dir = ContextCompat.getExternalFilesDirs(it, null)[i].absolutePath
                    val t1 = dir.indexOf("/Android/data/")
                    if (t1 != -1) dir = dir.substring(0, t1)
                    path = File(dir)
                    loadFileList()
                    (binding.content.adapter as TitleListAdaprer).notifyDataSetChanged()
                } else if (sdCard2 && i == 0) {
                    fileList.clear()
                    sdCard2 = false
                    sdCard = true
                    fileList.add(MyFile("Унутраная памяць", R.drawable.directory_icon))
                    if (files.size > 1) {
                        fileList.add(MyFile("Карта SD", R.drawable.directory_icon))
                    }
                    (binding.content.adapter as TitleListAdaprer).notifyDataSetChanged()
                } else {
                    sdCard2 = false
                    chosenFile = fileList[i].name
                    val sel = File(path.toString() + "/" + chosenFile)
                    if (sel.isDirectory) {
                        firstLvl = false
                        str.add(chosenFile)
                        path = File(sel.toString() + "")
                        loadFileList()
                        (binding.content.adapter as TitleListAdaprer).notifyDataSetChanged()
                    } else if (chosenFile == "Верх" && !sel.exists()) {
                        val s = str.removeAt(str.size - 1)
                        path = File(path.toString().substring(0, path.toString().lastIndexOf(s)))
                        if (str.isEmpty()) {
                            firstLvl = true
                        }
                        loadFileList()
                        (binding.content.adapter as TitleListAdaprer).notifyDataSetChanged()
                    } else {
                        mListener?.onDialogFile(sel)
                        alert.cancel()
                    }
                }
            }
        }
        return alert
    }

    private inner class TitleListAdaprer(private val mContext: Activity) : ArrayAdapter<MyFile>(mContext, R.layout.admin_simple_list_item, fileList) {
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val binding = AdminSimpleListItemBinding.inflate(LayoutInflater.from(context), parent, false)
                rootView = binding.root
                viewHolder = ViewHolder(binding.label)
                rootView.tag = viewHolder
            } else {
                rootView = mView
                viewHolder = rootView.tag as ViewHolder
            }
            viewHolder.text.text = fileList[position].name
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            val image = ContextCompat.getDrawable(mContext, fileList[position].resources)
            val density = resources.displayMetrics.density.toInt()
            image?.setBounds(0, 0, 48 * density, 48 * density)
            viewHolder.text.setCompoundDrawables(image, null, null, null)
            return rootView
        }

    }

    private class ViewHolder(var text: TextView)

    private class MyFile(val name: String, val resources: Int)
}