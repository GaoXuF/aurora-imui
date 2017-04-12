package cn.jiguang.imui.messages;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.jiguang.imui.BuildConfig;
import cn.jiguang.imui.R;
import cn.jiguang.imui.commons.BitmapLoader;
import cn.jiguang.imui.commons.models.IMessage;
import cn.jiguang.imui.utils.CircleImageView;
import cn.jiguang.imui.utils.DateFormatter;

public class PhotoViewHolder<MESSAGE extends IMessage> extends BaseMessageViewHolder<MESSAGE>
        implements MsgListAdapter.DefaultMessageViewHolder {

    private boolean mIsSender;
    private TextView mDateTv;
    private ImageView mPhotoIv;
    private CircleImageView mAvatarIv;
    private int mMaxWidth;
    private int mMaxHeight;


    public PhotoViewHolder(View itemView, boolean isSender) {
        super(itemView);
        this.mIsSender = isSender;
        mDateTv = (TextView) itemView.findViewById(R.id.aurora_tv_msgitem_date);
        mPhotoIv = (ImageView) itemView.findViewById(R.id.aurora_iv_msgitem_photo);
        mAvatarIv = (CircleImageView) itemView.findViewById(R.id.aurora_iv_msgitem_avatar);
    }

    @Override
    public void onBind(final MESSAGE message) {
        mDateTv.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
        boolean isAvatarExists = message.getUserInfo().getAvatar() != null
                && !message.getUserInfo().getAvatar().isEmpty();
        if (isAvatarExists && mImageLoader != null) {
            mImageLoader.loadImage(mAvatarIv, message.getUserInfo().getAvatar());
        }

        setPictureScale(message.getContentFile(), mPhotoIv);
        ViewGroup.LayoutParams params = mPhotoIv.getLayoutParams();
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int maxWidth = dm.widthPixels / 2;
        int maxHeight = dm.heightPixels / 3;
        Bitmap bitmap = BitmapLoader.getCompressBitmap(message.getContentFile(), maxWidth, maxHeight, mDensity);
        if (bitmap != null) {
            params.width = bitmap.getWidth();
            params.height = bitmap.getHeight();
            mPhotoIv.setLayoutParams(params);
            mPhotoIv.setImageBitmap(bitmap);
        }

        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAvatarClickListener != null) {
                    mAvatarClickListener.onAvatarClick(message);
                }
            }
        });

        mPhotoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMsgClickListener != null) {
                    mMsgClickListener.onMessageClick(message);
                }
            }
        });

        mPhotoIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mMsgLongClickListener != null) {
                    mMsgLongClickListener.onMessageLongClick(message);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w("MsgListAdapter", "Didn't set long click listener! Drop event.");
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void applyStyle(MessageListStyle style) {
        mDateTv.setTextSize(style.getDateTextSize());
        mDateTv.setTextColor(style.getDateTextColor());
        if (mIsSender) {
            mPhotoIv.setBackground(style.getSendPhotoMsgBg());
        } else {
            mPhotoIv.setBackground(style.getReceivePhotoMsgBg());
        }
        android.view.ViewGroup.LayoutParams layoutParams = mAvatarIv.getLayoutParams();
        layoutParams.width = style.getAvatarWidth();
        layoutParams.height = style.getAvatarHeight();
        mAvatarIv.setLayoutParams(layoutParams);
    }

    /**
     * 设置图片最小宽高
     *
     * @param path      图片路径
     * @param imageView 显示图片的View
     */
    private void setPictureScale(String path, ImageView imageView) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        //计算图片缩放比例
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        if (imageWidth < 100 * mDensity) {
            imageHeight = imageHeight * (100 * mDensity / imageWidth);
            imageWidth = 100 * mDensity;
        }
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) imageWidth;
        params.height = (int) imageHeight;
        imageView.setLayoutParams(params);
    }
}