package com.onyx.commander.gui;

import com.onyx.commander.communication.OnyxClient;
import com.onyx.common.commands.Command;

import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.concurrent.Future;

public class UpdateUiService extends ScheduledService<String> {

  private OnyxClient client;
  private Node[] nodes;

  private Label connStatusLbl;
  private Future<?> vlcdrFuture;
  private LineChart<Double, Double> engineSpeedChart;
  private Series<Double, Double> motor1Series;
  private Series<Double, Double> motor2Series;
  private Series<Double, Double> motor3Series;
  private Series<Double, Double> motor4Series;
  private TextArea commandOutputTextArea;
  
  /**
   * Create a new UI Update service.
   * @param client
   *    the connection client.
   * @param videoFuture
   *    the video future.
   * @param nodes
   *    the list of UI Nodes to be updated.
   */
  public UpdateUiService(OnyxClient client,Future<?> videoFuture, Node... nodes) {
    this.client = client;
    this.nodes = nodes;
    this.vlcdrFuture = videoFuture;
    init();
  }
  
  /**
   * Initialize the UI Nodes.
   */
  @SuppressWarnings("unchecked")
  public void init() {
    connStatusLbl = (Label) nodes[0];
    commandOutputTextArea = (TextArea) nodes[1];
    engineSpeedChart = (LineChart<Double, Double>) nodes[2];
    motor1Series = new XYChart.Series<Double, Double>();
    motor1Series.setName("Motor 1");
    motor2Series = new XYChart.Series<Double, Double>();
    motor2Series.setName("Motor 2");
    motor3Series = new XYChart.Series<Double, Double>();
    motor3Series.setName("Motor 3");
    motor4Series = new XYChart.Series<Double, Double>();
    motor4Series.setName("Motor 4");
    engineSpeedChart.getData().add(motor1Series);
    engineSpeedChart.getData().add(motor2Series);
    engineSpeedChart.getData().add(motor3Series);
    engineSpeedChart.getData().add(motor4Series);
    engineSpeedChart.setAnimated(true);
  }
  

  /**
   * Add the response pop to the Command response text area.
   * @param pop
   *      the response to be added to the text area.
   */
  private void addCommandResponse(String pop) {
    commandOutputTextArea.setText(commandOutputTextArea.getText() 
        + pop 
        + System.lineSeparator());
  }
  
  @Override
  protected Task<String> createTask() {
    return new Task<String>() {
      @Override
      protected String call() throws Exception {
        Platform.runLater( new Runnable() {
          @Override
          public void run() {
            if (client == null) {
              return;
            }
            if (client.isConnected()) {
              connStatusLbl.setText("Connection Status: connected.");
            }
            if (!client.isConnected()) {
              connStatusLbl.setText("Connection Status: disconnected.");
              vlcdrFuture.cancel(true);
            }
            Command msg =  client.getNextMessage();
            if (msg != null) {
              addCommandResponse(msg.getMessage().getContent());
            }
          }
        });
        return null;
      }
    };
  }
  
}
