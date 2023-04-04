//package com.example.mindline.adapters;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.mindline.R;
//import com.example.mindline.models.Memory;
//
//import java.util.List;
//
//public class MemoryListAdapter extends RecyclerView.Adapter<MemoryListAdapter.MemoryViewHolder> {
//
//    private List<Memory> memoryList;
//    private OnItemClickListener onItemClickListener;
//
//    public MemoryListAdapter(List<Memory> memoryList, OnItemClickListener onItemClickListener) {
//        this.memoryList = memoryList;
//        this.onItemClickListener = onItemClickListener;
//    }
//
//    public void setMemoryList(List<Memory> memoryList) {
//        this.memoryList = memoryList;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.memory_list_item, parent, false);
//        return new MemoryViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
//        Memory memory = memoryList.get(position);
//        holder.bind(memory, onItemClickListener);
//    }
//
//    @Override
//    public int getItemCount() {
//        return memoryList != null ? memoryList.size() : 0;
//    }
//
//    public static class MemoryViewHolder extends RecyclerView.ViewHolder {
//
//        TextView memoryTitle;
//
//        public MemoryViewHolder(@NonNull View itemView) {
//            super(itemView);
//            memoryTitle = itemView.findViewById(R.id.memory_title);
//        }
//
//        public void bind(Memory memory, OnItemClickListener onItemClickListener) {
//            memoryTitle.setText(memory.getTitle());
//            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(memory));
//        }
//    }
//
//    public interface OnItemClickListener {
//        void onItemClick(Memory memory);
//    }
//
//    public List<Memory> getMemoryList() {
//        return memoryList;
//    }
//}
