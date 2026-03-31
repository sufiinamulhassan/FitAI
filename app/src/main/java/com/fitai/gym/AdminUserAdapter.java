package com.fitai.gym;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private Context context;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditUser(UserModel user, int position);
        void onDeleteUser(UserModel user, int position);
    }

    public AdminUserAdapter(List<UserModel> userList) {
        this.userList = userList;
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        String role = user.getRole() != null ? user.getRole() : "user";
        String goal = user.getGoal() != null ? user.getGoal() : "No Goal Set";
        holder.tvGoal.setText(role.toUpperCase() + " · " + goal);

        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
            if (!user.getProfilePicUrl().startsWith("http")) {
                android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(user.getProfilePicUrl());
                if (bitmap != null) holder.ivProfile.setImageBitmap(bitmap);
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.pp_1);
        }

        // Long press to show actions
        holder.itemView.setOnLongClickListener(v -> {
            showActionDialog(user, position);
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            showActionDialog(user, position);
        });
    }

    private void showActionDialog(UserModel user, int position) {
        new AlertDialog.Builder(context)
            .setTitle(user.getName())
            .setItems(new String[]{"Edit User", "Delete User", "View Progress", "Cancel"}, (d, which) -> {
                switch (which) {
                    case 0:
                        if (listener != null) listener.onEditUser(user, position);
                        break;
                    case 1:
                        confirmDelete(user, position);
                        break;
                    case 2:
                        Toast.makeText(context, "Progress viewer coming soon!", Toast.LENGTH_SHORT).show();
                        break;
                }
            })
            .show();
    }

    private void confirmDelete(UserModel user, int position) {
        new AlertDialog.Builder(context)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete " + user.getName() + "?\nThis will remove their data from the database.")
            .setPositiveButton("Delete", (d, w) -> {
                if (listener != null) listener.onDeleteUser(user, position);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvName, tvEmail, tvGoal;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivUserImage);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvGoal = itemView.findViewById(R.id.tvUserGoal);
        }
    }
}
