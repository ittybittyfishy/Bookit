package com.example.booknook.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var name: EditText
    private lateinit var gender: EditText
    private lateinit var birthday: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedButton : Button = view.findViewById(R.id.saveButton)
        newPassword = view.findViewById(R.id.newPassword)
        confirmPassword = view.findViewById(R.id.confirmedPassword)
        currentPassword = view.findViewById(R.id.currentPassword)
        name = view.findViewById(R.id.name_edit)
        gender = view.findViewById(R.id.genderEdit)
        birthday = view.findViewById((R.id.birthdayEditText))

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        savedButton.setOnClickListener{
            if (newPassword.text.isNotEmpty() || currentPassword.text.isNotEmpty() || confirmPassword.text.isNotEmpty())
            {
                changePassword()
            }
            else if (name.text.isNotEmpty())
            {
                changeName()
            }
            else if (gender.text.isNotEmpty())
            {
                changeGender()
            }
        }
    }

    private fun changePassword() {
        val newPassword = newPassword.text.toString()
        val confirmPassword = confirmPassword.text.toString()

        // Check if new password and confirm password match
        if (newPassword != confirmPassword) {
            // Show an error message or toast indicating passwords don't match
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        else if (newPassword.length < 6)
        {
            Toast.makeText(requireContext(), "New password too short", Toast.LENGTH_SHORT).show()
            return
        }

        else if (currentPassword.text.toString() == newPassword)
        {
            Toast.makeText(requireContext(), "New password cannot be the same password", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user
        val user = auth.currentUser

        user?.let {
            // Call reauthenticate method to reauthenticate the user
            val credential =
                EmailAuthProvider.getCredential(user.email!!, currentPassword.text.toString())
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful)
                    {
                        // Reauthentication successful, now update the password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful)
                                {
                                    // Password updated successfully
                                    Toast.makeText(
                                        requireContext(),
                                        "Password updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else
                                {
                                    // Password update failed, show error message
                                    Toast.makeText(
                                        requireContext(),
                                        "Password update failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                    else
                    {
                        // Reauthentication failed, show error message
                        Toast.makeText(
                            requireContext(),
                            "Reauthentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun changeName() {
        val newName = name.text.toString()
        // Get the current user
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).update("name",newName).addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Name updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error updating name: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun changeGender()
    {
        val newGender = gender.text.toString()
        // Get the current user
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).update("gender",newGender).addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Gender updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error updating Gender: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}