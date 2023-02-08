package com.example.pkartadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pkartadmin.R
import com.example.pkartadmin.databinding.ItemCategoryLayoutBinding
import com.example.pkartadmin.model.Categorymodel

class CategoryAdapter(var context: Context, val list: ArrayList<Categorymodel>) :
    RecyclerView.Adapter<CategoryAdapter.categoryViewHolder>() {

    inner class categoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var binding = ItemCategoryLayoutBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoryViewHolder {
        return categoryViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_category_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: categoryViewHolder, position: Int) {

        holder.binding.ItemText.text = list[position].cate
        Glide.with(context).load(list[position].img).into(holder.binding.itemImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}