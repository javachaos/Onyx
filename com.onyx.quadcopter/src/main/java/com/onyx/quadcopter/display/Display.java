package com.onyx.quadcopter.display;

import com.onyx.common.utils.Constants;
import com.onyx.common.utils.ExceptionUtils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;

public class Display {

  /**
   * Logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(Display.class);

  /**
   * Buffer size.
   */
  private static final int BUFFER_SIZE = 16;

  /**
   * OLED Data address register.
   */
  private static final int DATA_ADDRESS = 0x40;

  protected int vccState;
  protected BufferedImage img;
  protected Graphics2D graphics;
  private int width;
  private int height;
  private int pages;
  private boolean usingI2C;
  private boolean hasRst;
  private GpioPinDigitalOutput rstPin;
  private GpioPinDigitalOutput dcPin;
  private I2CDevice i2c;
  private SpiDevice spi;
  private byte[] buffer;

  /**
   * Display object using SPI communication with a reset pin
   *
   * @param width Display width
   * @param height Display height
   * @param gpio GPIO object
   * @param spi SPI device
   * @param rstPin Reset pin
   * @param dcPin Data/Command pin
   * @see GpioFactory#getInstance() GpioController instance factory
   * @see com.pi4j.io.spi.SpiFactory#getInstance(SpiChannel) SpiDevice factory
   */
  public Display(int width, int height, GpioController gpio, SpiDevice spi, Pin rstPin, Pin dcPin) {
    this(width, height, false, gpio, rstPin);

    this.dcPin = gpio.provisionDigitalOutputPin(dcPin);
    this.spi = spi;
  }

  /**
   * Display object using I2C communication with a reset pin <br/>
   * As I haven't got an I2C display and I don't understand I2C much, I just tried to copy the
   * Adafruit's library and I am using a hack to use WiringPi function similar to one in the
   * original lib directly.
   *
   * @param width Display width
   * @param height Display height
   * @param gpio GPIO object
   * @param i2c I2C object
   * @param address Display address
   * @param rstPin Reset pin
   * @see GpioFactory#getInstance() GpioController instance factory
   * @see com.pi4j.io.i2c.I2CFactory#getInstance(int) I2C bus factory
   * @throws ReflectiveOperationException Thrown if I2C handle is not accessible
   * @throws IOException Thrown if the bus can't return device for specified address
   */
  public Display(int width, int height, GpioController gpio, I2CBus i2c, int address, Pin rstPin)
      throws ReflectiveOperationException, IOException {
    this(width, height, true, gpio, rstPin);
    this.i2c = i2c.getDevice(address);
  }

  /**
   * Display object using SPI communication without a reset pin
   *
   * @param width Display width
   * @param height Display height
   * @param gpio GPIO object
   * @param spi SPI device
   * @param dcPin Data/Command pin
   * @see Display#Display(int, int, GpioController, SpiDevice, Pin, Pin) Using this constructor with
   *      null Pin
   * @see GpioFactory#getInstance() GpioController instance factory
   * @see com.pi4j.io.spi.SpiFactory#getInstance(SpiChannel) SpiDevice factory
   */
  public Display(int width, int height, GpioController gpio, SpiDevice spi, Pin dcPin) {
    this(width, height, gpio, spi, null, dcPin);
  }

  /**
   * Display object using I2C communication without a reset pin
   *
   * @param width Display width
   * @param height Display height
   * @param gpio GPIO object
   * @param i2c I2C object
   * @param address Display address
   * @see Display#Display(int, int, GpioController, I2CBus, int, Pin) Using this constructor with
   *      null Pin
   * @see GpioFactory#getInstance() GpioController instance factory
   * @see com.pi4j.io.i2c.I2CFactory#getInstance(int) I2C bus factory
   */
  public Display(int width, int height, GpioController gpio, I2CBus i2c, int address)
      throws ReflectiveOperationException, IOException {
    this(width, height, gpio, i2c, address, null);
  }

  private Display(int width, int height, boolean i2c, GpioController gpio, Pin rstPin) {
    this.width = width;
    this.height = height;
    this.pages = (height / 8);
    this.buffer = new byte[width * this.pages];
    this.usingI2C = i2c;

    if (rstPin != null) {
      this.rstPin = gpio.provisionDigitalOutputPin(rstPin);
      this.hasRst = true;
    } else {
      this.hasRst = false;
    }

    this.img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
    this.graphics = this.img.createGraphics();
  }

  private void initDisplay() {
    if (this.width == 128 && this.height == 64) {
      this.init(0x3F, 0x12, 0x80);
    } else if (this.width == 128 && this.height == 32) {
      this.init(0x1F, 0x02, 0x80);
    } else if (this.width == 96 && this.height == 16) {
      this.init(0x0F, 0x02, 0x60);
    }

  }

  private void init(int multiplex, int compins, int ratio) {
    this.command(DisplayConstants.SSD1306_DISPLAYOFF);
    this.command(DisplayConstants.SSD1306_SETDISPLAYCLOCKDIV);
    this.command((short) ratio);
    this.command(DisplayConstants.SSD1306_SETMULTIPLEX);
    this.command((short) multiplex);
    this.command(DisplayConstants.SSD1306_SETDISPLAYOFFSET);
    this.command((short) 0x0);
    this.command(DisplayConstants.SSD1306_SETSTARTLINE | 0x0);
    this.command(DisplayConstants.SSD1306_CHARGEPUMP);

    if (this.vccState == DisplayConstants.SSD1306_EXTERNALVCC) {
      this.command((short) 0x10);
    } else {
      this.command((short) 0x14);
    }

    this.command(DisplayConstants.SSD1306_MEMORYMODE);
    this.command((short) 0x00);
    this.command((short) (DisplayConstants.SSD1306_SEGREMAP | 0x1));
    this.command(DisplayConstants.SSD1306_COMSCANDEC);
    this.command(DisplayConstants.SSD1306_SETCOMPINS);
    this.command((short) compins);
    this.command(DisplayConstants.SSD1306_SETCONTRAST);
    if (this.vccState == DisplayConstants.SSD1306_EXTERNALVCC) {
      this.command((short) 0x9F);
    } else {
      this.command((short) 0xCF);
    }

    this.command(DisplayConstants.SSD1306_SETPRECHARGE);

    if (this.vccState == DisplayConstants.SSD1306_EXTERNALVCC) {
      this.command((short) 0x22);
    } else {
      this.command((short) 0xF1);
    }

    this.command(DisplayConstants.SSD1306_SETVCOMDETECT);
    this.command((short) 0x40);
    this.command(DisplayConstants.SSD1306_DISPLAYALLON_RESUME);
    this.command(DisplayConstants.SSD1306_NORMALDISPLAY);
  }

  /**
   * Turns on command mode and sends command
   * 
   * @param command Command to send. Should be in short range.
   */
  public void command(int command) {
    if (this.usingI2C) {
      this.i2cWrite(0, command);
    } else {
      this.dcPin.setState(false);
      try {
        this.spi.write((short) command);
      } catch (IOException e1) {
        ExceptionUtils.logError(getClass(), e1);
      }
    }
  }

  /**
   * Turns on data mode and sends data array.
   * 
   * @param data Data array
   */
  public void data(byte[] data) {
    if (this.usingI2C) {
      byte[] buff = new byte[BUFFER_SIZE];
      for (int i = 0; i < data.length; i += BUFFER_SIZE) {
        System.arraycopy(data, i, buff, 0, BUFFER_SIZE);
        try {
          i2c.write(DATA_ADDRESS, buff, 0, BUFFER_SIZE);
        } catch (IOException e1) {
          ExceptionUtils.logError(getClass(), e1);
        }
      }
      buff = null;
    } else {
      this.dcPin.setState(true);
      try {
        this.spi.write(data);
      } catch (IOException e1) {
        ExceptionUtils.logError(getClass(), e1);
      }
    }
  }

  /**
   * Begin with SWITCHCAPVCC VCC mode.
   * 
   * @see DisplayConstants#SSD1306_SWITCHCAPVCC
   */
  public void begin() {
    this.begin(DisplayConstants.SSD1306_SWITCHCAPVCC);
  }

  /**
   * Begin with specified VCC mode (can be SWITCHCAPVCC or EXTERNALVCC).
   * 
   * @param vccState VCC mode
   * @see DisplayConstants#SSD1306_SWITCHCAPVCC
   * @see DisplayConstants#SSD1306_EXTERNALVCC
   */
  public void begin(int vccState) {
    this.vccState = vccState;
    this.reset();
    this.initDisplay();
    this.command(DisplayConstants.SSD1306_DISPLAYON);
    this.clear();
    this.display();
  }

  /**
   * Pulls reset pin high and low and resets the display.
   */
  public void reset() {
    if (this.hasRst) {
      try {
        this.rstPin.setState(true);
        Thread.sleep(1);
        this.rstPin.setState(false);
        Thread.sleep(10);
        this.rstPin.setState(true);
      } catch (InterruptedException e1) {
        ExceptionUtils.logError(getClass(), e1);
      }
    }
  }

  /**
   * Sends the buffer to the display.
   */
  public synchronized void display() {
    this.command(DisplayConstants.SSD1306_COLUMNADDR);
    this.command(0);
    this.command(this.width - 1);
    this.command(DisplayConstants.SSD1306_PAGEADDR);
    this.command(0);
    this.command(this.pages - 1);
    this.data(this.buffer);
  }

  /**
   * Clears the buffer by creating a new byte array.
   */
  public void clear() {
    this.xpos = Constants.OLED_X_START;
    this.ypos = Constants.OLED_Y_START;
    this.buffer = new byte[this.width * this.pages];
    this.img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
    this.graphics = this.img.createGraphics();
    displayImage();
  }

  /**
   * Sets the display contract. Apparently not really working.
   * 
   * @param contrast Contrast
   */
  public void setContrast(byte contrast) {
    this.command(DisplayConstants.SSD1306_SETCONTRAST);
    this.command(contrast);
  }

  /**
   * Sets if the backlight should be dimmed.
   * 
   * @param dim Dim state
   */
  public void dim(boolean dim) {
    if (dim) {
      this.setContrast((byte) 0);
    } else {
      if (this.vccState == DisplayConstants.SSD1306_EXTERNALVCC) {
        this.setContrast((byte) 0x9F);
      } else {
        this.setContrast((byte) 0xCF);
      }
    }
  }

  /**
   * Sets if the display should be inverted.
   * 
   * @param invert Invert state
   */
  public void invertDisplay(boolean invert) {
    if (invert) {
      this.command(DisplayConstants.SSD1306_INVERTDISPLAY);
    } else {
      this.command(DisplayConstants.SSD1306_NORMALDISPLAY);
    }
  }

  /**
   * Probably broken.
   */
  public void scrollHorizontally(boolean left, int start, int end) {
    this.command(left ? DisplayConstants.SSD1306_LEFT_HORIZONTAL_SCROLL
        : DisplayConstants.SSD1306_RIGHT_HORIZONTAL_SCROLL);
    this.command(0);
    this.command(start);
    this.command(0);
    this.command(end);
    this.command(1);
    this.command(0xFF);
    this.command(DisplayConstants.SSD1306_ACTIVATE_SCROLL);
  }

  /**
   * Probably broken.
   */
  public void scrollDiagonally(boolean left, int start, int end) {
    this.command(DisplayConstants.SSD1306_SET_VERTICAL_SCROLL_AREA);
    this.command(0);
    this.command(this.height);
    this.command(left ? DisplayConstants.SSD1306_VERTICAL_AND_LEFT_HORIZONTAL_SCROLL
        : DisplayConstants.SSD1306_VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL);
    this.command(0);
    this.command(start);
    this.command(0);
    this.command(end);
    this.command(1);
    this.command(DisplayConstants.SSD1306_ACTIVATE_SCROLL);
  }

  /**
   * Stops scrolling.
   */
  public void stopScroll() {
    this.command(DisplayConstants.SSD1306_DEACTIVATE_SCROLL);
  }

  /**
   * Get Width. 
   * 
   * @return Display width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Get height.
   * 
   * @return Display height
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Sets one pixel in the current buffer.
   * 
   * @param xpos X position
   * @param ypos Y position
   * @param white White or black pixel
   * @return True if the pixel was successfully set
   */
  public boolean setPixel(final int xpos, final int ypos, final boolean white) {
    if (xpos < 0 || xpos > this.width || ypos < 0 || ypos > this.height) {
      return false;
    }

    if (white) {
      this.buffer[xpos + (ypos / 8) * this.width] |= (1 << (ypos & 7));
    } else {
      this.buffer[xpos + (ypos / 8) * this.width] &= ~(1 << (ypos & 7));
    }

    return true;
  }

  /**
   * Copies AWT image contents to buffer. Calls display()
   * 
   * @see Display#display()
   */
  public synchronized void displayImage() {
    Raster rast = this.img.getRaster();
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.width; x++) {
        this.setPixel(x, y, (rast.getSample(x, y, 0) > 0));
      }
    }

    this.display();
  }

  /**
   * Sets internal buffer.
   * 
   * @param buffer New used buffer
   */
  public void setBuffer(byte[] buffer) {
    this.buffer = buffer;
  }

  /**
   * Sets one byte in the buffer.
   * 
   * @param position Position to set
   * @param value Value to set
   */
  public void setBufferByte(int position, byte value) {
    this.buffer[position] = value;
  }

  /**
   * Sets internal AWT image to specified one.
   * 
   * @param img BufferedImage to set
   * @param createGraphics If true, createGraphics() will be called on the image and the result will
   *        be saved to the internal Graphics field accessible by getGraphics() method
   */
  public void setImage(BufferedImage img, boolean createGraphics) {
    this.img = img;

    if (createGraphics) {
      this.graphics = img.createGraphics();
    }
  }

  /**
   * Returns internal AWT image.
   * 
   * @return BufferedImage
   */
  public BufferedImage getImage() {
    return this.img;
  }

  /**
   * Returns Graphics object which is associated to current AWT image, if it wasn't set using
   * setImage() with false createGraphics parameter.
   * 
   * @return Graphics2D object
   */
  public Graphics2D getGraphics() {
    return this.graphics;
  }

  private void i2cWrite(final int register, final int value) {
    try {
      i2c.write(register, (byte) (value & 0xFF));
    } catch (IOException e1) {
      ExceptionUtils.logError(getClass(), e1);
    }
  }

  private int xpos = Constants.OLED_X_START;
  private int ypos = Constants.OLED_Y_START;

  private String displayedString;

  /**
   * Write text to the screen.
   * 
   * @param string
   *    the string to write.
   */
  public void write(String string) {
    if (string != null && !string.isEmpty() && !string.equals(displayedString)) {
      displayedString = string;
      clear();
      getGraphics().setColor(Color.WHITE);
      getGraphics().setFont(new Font("Monospaced", Font.PLAIN, Constants.DISP_FONT));
      drawStringMultiLine(displayedString);
      displayImage();
    }
  }

  /**
   * Draw a string to the graphics for this display.
   */
  private void drawString(String str, int xpos, int ypos) {
    FontMetrics metrics = getGraphics().getFontMetrics();
    getGraphics().drawString(str, xpos, ypos + metrics.getHeight());
  }

  private synchronized void drawStringMultiLine(String text) {
    FontMetrics metrics = getGraphics().getFontMetrics();
    String[] lines = text.split(System.lineSeparator());
    for (String line : lines) {
      if (lines.length == 1) {
        break;
      }
      drawStringMultiLine(line);
      ypos += metrics.getHeight();
    }
    if (metrics.stringWidth(text) < width) {
      drawString(text, xpos, ypos);
    } else {
      String[] words = text.split(" ");
      String currentLine = words[0];
      for (int i = 1; i < words.length; i++) {
        if (metrics.stringWidth(currentLine + words[i]) < width) {
          currentLine += " " + words[i];
        } else {
          drawString(currentLine, xpos, ypos);
          ypos += metrics.getHeight();
          currentLine = words[i];
        }
      }

      if (currentLine.length() > 0) {
        drawString(currentLine, xpos, ypos);
      }
    }
  }
}
