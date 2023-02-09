package com.example.pkartadmin.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pkartadmin.R
import com.example.pkartadmin.adapter.AddProductImageAdapter
import com.example.pkartadmin.databinding.FragmentAddProductBinding
import com.example.pkartadmin.model.AddProductModel
import com.example.pkartadmin.model.Categorymodel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImageUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>


    private var launchGallaryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {
            coverImage = it.data!!.data

            binding.productCoverImage.setImageURI(coverImage)
            binding.productCoverImage.visibility = VISIBLE

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val imageUrl = it.data!!.data
        list.add(imageUrl!!)
        adapter.notifyDataSetChanged()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(layoutInflater)

        list = ArrayList()
        listImages = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.btnSelectCover.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGallaryActivity.launch(intent)

        }
        binding.btnProductImage.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)

        }

        setProductCategory()

        adapter = AddProductImageAdapter(list)
        binding.productImageRecycler.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            validateData()
        }


        return binding.root


    }

    private fun validateData() {
        if (binding.productNameEdit.text.toString().isEmpty()) {

            binding.productNameEdit.requestFocus()
            binding.productNameEdit.error = "EmptyName"
        } else if (binding.productSpEdit.text.toString().isEmpty()) {
            binding.productSpEdit.requestFocus()
            binding.productSpEdit.error = "EmptySp"
        } else if (binding.productMrpEdit.text.toString().isEmpty()) {
            binding.productMrpEdit.requestFocus()
            binding.productMrpEdit.error = "EmptyMrp"
        } else if (binding.productDicripEdit.text.toString().isEmpty()) {
            binding.productMrpEdit.requestFocus()
            binding.productMrpEdit.error = "EmptyDescription"
        } else if (coverImage == null) {
            Toast.makeText(requireContext(), "Please select cover Image", Toast.LENGTH_SHORT).show()
        } else if (list.size < 1) {
            Toast.makeText(requireContext(), "Please select product Images", Toast.LENGTH_SHORT)
                .show()

        } else {
            uploadImage()
        }

    }

    private fun uploadImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImageUrl = image.toString()

                    uploadProductImage()
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong1", Toast.LENGTH_SHORT).show()

            }
    }


    private var i = 0
    private fun uploadProductImage() {
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())


                    if (list.size == listImages.size) {
                        storeData()
                    } else {
                        i += 1
                        uploadProductImage()
                    }
                }

            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong1", Toast.LENGTH_SHORT).show()

            }
    }

    private fun storeData() {

        val db = Firebase.firestore.collection("products")
        val key = db.id

        val data = AddProductModel(
            binding.productNameEdit.text.toString(),
            binding.productDicripEdit.text.toString(),
            coverImageUrl.toString(),
            categoryList[binding.productCateDropdown.selectedItemPosition],
            key,
            binding.productMrpEdit.text.toString(),
            binding.productSpEdit.text.toString(),
            listImages

        )

        db.document(key).set(data).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Product added", Toast.LENGTH_SHORT).show()
            binding.productNameEdit.text = null
            binding.productDicripEdit.text = null
            binding.productMrpEdit.text = null
            binding.productSpEdit.text = null
        }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "something went wrong", Toast.LENGTH_SHORT).show()

            }

    }

    private fun setProductCategory() {

        categoryList = ArrayList()
        Firebase.firestore.collection("Categories").get().addOnSuccessListener {

            categoryList.clear()
            for (doc in it.documents) {
                val data = doc.toObject(Categorymodel::class.java)
                categoryList.add(data!!.cate!!)

                categoryList.add(0, "Select Category")
                val arrAdapter =
                    ArrayAdapter(requireContext(), R.layout.dropdown_item_layout, categoryList)

                binding.productCateDropdown.adapter = arrAdapter

            }
        }
    }


}