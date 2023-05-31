package edu.skku.cs.afinal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

class RemoveFileAdapter constructor(val context: Context, val items : List<FileInfoData>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }
    override fun getItem(position: Int): Any {
        return items.get(position)
    }
    override fun getItemId(position: Int): Long {
        return 0
    }
    override fun getView(i: Int, cvtView: View?, parent: ViewGroup?): View {
        val inflater : LayoutInflater =
            LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.file, null)
        val fileName = items.get(i).fileName
        val content = items.get(i).content
        val creationTime = items.get(i).creationTime

        var contentTextView = view.findViewById<TextView>(R.id.contentTextView)
        var creationTimeTextView = view.findViewById<TextView>(R.id.creationTimeTextView)
        var checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        contentTextView.text = content
        creationTimeTextView.text = creationTime
        checkBox.setOnCheckedChangeListener { _, b ->
            if(b){
                FileRemoveActivity.checkedFiles.add(fileName)
            }
            else{
                FileRemoveActivity.checkedFiles.remove(fileName)
            }
        }
        return view
    }

}