package com.d2lcoder.linear.regression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.d2lcoder.linear.regression.utils.DataPoints;

import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.ParameterList;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Batch;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;

public class LinearRegression {
	public static void main(String[] args) throws IOException, TranslateException {
		NDManager manager = NDManager.newBaseManager();

		NDArray trueW = manager.create(new float[]{2, -3.4f});
		float trueB = 4.2f;

		DataPoints dp = DataPoints.syntheticData(manager, trueW, trueB, 1000);
		NDArray features = dp.getX();
		NDArray labels = dp.getY();
		
		System.out.println("features:" + features.get("0:2"));
		System.out.println("labels:" + labels.get("0:2"));
		
		
		//定义模式，一层神经网络
		Model model = Model.newInstance("lin-reg");
		SequentialBlock net = new SequentialBlock();
		Linear linearBlock = Linear.builder().optBias(true).setUnits(1).build();
		net.add(linearBlock);
		model.setBlock(net);
		
		//损失函数
		Loss l2loss = Loss.l2Loss();

		//随机梯度下降 sgd
		Tracker lrt = Tracker.fixed(0.03f);
		Optimizer sgd = Optimizer.sgd().setLearningRateTracker(lrt).build();
		
		
		//设置Training
		DefaultTrainingConfig config = new DefaultTrainingConfig(l2loss)
			    .optOptimizer(sgd)
			    .addTrainingListeners(TrainingListener.Defaults.logging()); // Logging

		Trainer trainer = model.newTrainer(config);
		
		int batchSize = 10;
		trainer.initialize(new Shape(batchSize, 2));

		
		//
		Metrics metrics = new Metrics();
		trainer.setMetrics(metrics);
		
		
		ArrayDataset  dataset = loadArray(features, labels, batchSize, false);
		int numEpochs = 2;

		for (int epoch = 1; epoch <= numEpochs; epoch++) {
		    System.out.printf("Epoch %d\n", epoch);
		    // Iterate over dataset
		    for (Batch batch : trainer.iterateDataset(dataset)) {
		        // Update loss and evaulator
		        EasyTrain.trainBatch(trainer, batch);

		        // Update parameters
		        trainer.step();

		        batch.close();
		    }
		    // reset training and validation evaluators at end of epoch
		    trainer.notifyListeners(listener -> listener.onEpoch(trainer));
		}
		
		
		//预测
		Block layer = model.getBlock();
		ParameterList params = layer.getParameters();
		NDArray wParam = params.valueAt(0).getArray();
		NDArray bParam = params.valueAt(1).getArray();

		float[] w = trueW.sub(wParam.reshape(trueW.getShape())).toFloatArray();
		System.out.printf("Error in estimating w: [%f %f]\n", w[0], w[1]);
		System.out.println(String.format("Error in estimating b: %f\n", trueB - bParam.getFloat()));
		
		
		//保存模型
		Path modelDir = Paths.get("./models/lin-reg");
		Files.createDirectories(modelDir);
		model.setProperty("Epoch", Integer.toString(numEpochs)); // save epochs trained as metadata
		model.save(modelDir, "lin-reg");
	}
	
	public static  ArrayDataset loadArray(NDArray features, NDArray labels, int batchSize, boolean shuffle) {
	    return new ArrayDataset.Builder()
	                  .setData(features) // 设置训练集
	                  .optLabels(labels) // 设置预测值
	                  .setSampling(batchSize, shuffle) // 设置批量大小和随机抽样
	                  .build();
	}
}
