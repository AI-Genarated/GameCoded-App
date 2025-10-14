
package com.example.gamecoded;

import android.content.Context;
import android.content.res.AssetManager;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnxxModelRunner {
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final OrtSession session;
    private final Map<String, Integer> vocab;
    private final Map<Integer, String> idToToken;

    // Constructor now requires a Context
    public OnxxModelRunner(Context context) {
        try {
            // Get the file path by copying the model from assets to the cache
            String modelPath = assetFilePath(context, "model.onnx");
            session = env.createSession(modelPath, new OrtSession.SessionOptions());

            InputStream vocabStream = context.getAssets().open("vocab.txt");
            vocab = new HashMap<>();
            idToToken = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(vocabStream))) {
                String line;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    vocab.put(line, index);
                    idToToken.put(index, line);
                    index++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OnnxModelRunner", e);
        }
    }

    // Helper function to copy a file from assets to the cache directory
    private String assetFilePath(Context context, String assetName) throws Exception {
        File file = new File(context.getCacheDir(), assetName);
        try (InputStream is = context.getAssets().open(assetName); FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return file.getAbsolutePath();
    }


    private int[] tokenize(String text) {
        List<String> tokens = Arrays.asList(text.toLowerCase().split(" "));
        return tokens.stream().mapToInt(token -> vocab.getOrDefault(token, vocab.get("[UNK]"))).toArray();
    }

    public String runInference(String inputText) {
        try {
            int[] tokenIds = tokenize(inputText);
            long[] inputIds = new long[tokenIds.length];
            for (int i = 0; i < tokenIds.length; i++) {
                inputIds[i] = tokenIds[i];
            }

            long[] attentionMask = new long[inputIds.length];
            Arrays.fill(attentionMask, 1);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), new long[]{1, inputIds.length}));
            inputs.put("attention_mask", OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), new long[]{1, inputIds.length}));

            try (OrtSession.Result results = session.run(inputs)) {
                // Shape: [batch_size, sequence_length, vocab_size]
                float[][][] outputLogits = (float[][][]) results.get(0).getValue();
                StringBuilder resultString = new StringBuilder();

                // Perform argmax on the last dimension to get the predicted token ID
                for (float[] tokenLogits : outputLogits[0]) {
                    int predictedTokenId = -1;
                    float maxLogit = Float.NEGATIVE_INFINITY;
                    for (int i = 0; i < tokenLogits.length; i++) {
                        if (tokenLogits[i] > maxLogit) {
                            maxLogit = tokenLogits[i];
                            predictedTokenId = i;
                        }
                    }
                    if (predictedTokenId != -1) {
                        resultString.append(idToToken.getOrDefault(predictedTokenId, "")).append(" ");
                    }
                }
                return resultString.toString().trim();
            }
        } catch (OrtException e) {
            throw new RuntimeException("Failed to run inference", e);
        }
    }
}
