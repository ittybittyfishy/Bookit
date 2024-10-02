package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.booknook.BookItemCollection
import com.example.booknook.R

class EditTagsFragment(private val book: BookItemCollection, private val onTagsAdded: (List<String>) -> Unit) : DialogFragment() {
    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_tags, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tag1EditText: EditText = view.findViewById(R.id.tag1EditText)
        val tag2EditText: EditText = view.findViewById(R.id.tag2EditText)
        val tag3EditText: EditText = view.findViewById(R.id.tag3EditText)

        // Autofill existing tags if they exist
        val tags = book.tags
        if (tags.isNotEmpty()) {
            if (tags.size > 0) tag1EditText.setText(tags[0])
            if (tags.size > 1) tag2EditText.setText(tags[1])
            if (tags.size > 2) tag3EditText.setText(tags[2])
        }

        view.findViewById<Button>(R.id.confirmTagsButton).setOnClickListener {
            // Collect tags
            val newTags = mutableListOf<String>()
            if (tag1EditText.text.isNotEmpty()) newTags.add(tag1EditText.text.toString())
            if (tag2EditText.text.isNotEmpty()) newTags.add(tag2EditText.text.toString())
            if (tag3EditText.text.isNotEmpty()) newTags.add(tag3EditText.text.toString())

            // Call the callback
            onTagsAdded(newTags)

            dismiss()
        }

        view.findViewById<Button>(R.id.cancelTagsButton).setOnClickListener {
            dismiss() // Dismiss the dialog without saving tags
        }
    }

}