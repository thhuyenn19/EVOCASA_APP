package com.mobile.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BehaviorLogger {
    public static void record(
            String customerId,
            String productId,
            String actionType, // "wishlist", "review", "order", etc.
            String source,     // "homepage", "product_detail", etc.
            Integer rating     // nullable: dùng cho review
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String behaviorId = UUID.randomUUID().toString();

        Map<String, Object> data = new HashMap<>();

        // Lưu customer_id dưới dạng $oid
        Map<String, Object> customerOid = new HashMap<>();
        customerOid.put("$oid", customerId);
        data.put("customer_id", customerOid);

        // Lưu product_id dưới dạng $oid
        Map<String, Object> productOid = new HashMap<>();
        productOid.put("$oid", productId);
        data.put("product_id", productOid);

        data.put("action_type", actionType);

        // Lưu timestamp dưới dạng $date
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("$date", new Date());
        data.put("timestamp", timestamp);

        data.put("source", source);

        if (rating != null && actionType.equals("review")) {
            data.put("rating", rating);
        }


        db.collection("CustomerBehavior")
                .document(behaviorId)
                .set(data)
                .addOnSuccessListener(aVoid ->
                        android.util.Log.d("BehaviorLog", "✅ Logged: " + actionType + " for " + productId)
                )
                .addOnFailureListener(e ->
                        android.util.Log.e("BehaviorLog", "❌ Failed", e)
                );
    }
}
