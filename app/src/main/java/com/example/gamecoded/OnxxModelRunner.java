
package com.example.gamecoded;

import android.content.res.AssetManager;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OnxxModelRunner {
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final OrtSession session;
    private final Map<String, Integer> vocab;//c

    public OnxxModelRunner(AssetManager assetManager) {
        try {
            InputStream modelStream = assetManager.open("model.onnx");
            byte[] modelBytes = new byte[modelStream.available()];
            modelStream.read(modelBytes);
            modelStream.close();
            session = env.createSession(modelBytes, new OrtSession.SessionOptions());

            InputStream vocabStream = assetManager.open("vocab.txt");
            vocab = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(vocabStream))) {
                String line;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    vocab.put(line, index++);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OnnxModelRunner", e);
        }
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
                float[][][] output = (float[][][]) results.get(0).getValue();
                // This is a placeholder for how you might process the output.
                // You will need to replace this with your actual post-processing logic.
                StringBuilder resultString = new StringBuilder();
                for (float[][] matrix : output) {
                    for (float[] row : matrix) {
                        resultString.append(Arrays.toString(row)).append("\n");
                    }
                }
                return resultString.toString();
            }
        } catch (OrtException e) {
            throw new RuntimeException("Failed to run inference", e);
        }
    }
}
