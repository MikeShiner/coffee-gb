package eu.rekawek.coffeegb.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import eu.rekawek.coffeegb.Gameboy;
import eu.rekawek.coffeegb.GameboyOptions;
import eu.rekawek.coffeegb.controller.QueueController;
import eu.rekawek.coffeegb.cpu.SpeedMode;
import eu.rekawek.coffeegb.debug.Console;
import eu.rekawek.coffeegb.gpu.Display;
import eu.rekawek.coffeegb.jms.ActionListener;
import eu.rekawek.coffeegb.jms.JmsConfiguration;
import eu.rekawek.coffeegb.memory.cart.Cartridge;
import eu.rekawek.coffeegb.serial.SerialEndpoint;
import eu.rekawek.coffeegb.sound.SoundOutput;

public class Emulator {

    private static final int SCALE = 2;
    
    // TODO: Add to external configuration
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String INSTRUCTION_QUEUE = "GB_INSTRUCTION";

    private final GameboyOptions options;

    private final Cartridge rom;

    private final AudioSystemSoundOutput sound;

    private final SwingDisplay display;

    private final SwingController guiController;
    private final QueueController queueController;

    private final SerialEndpoint serialEndpoint;

    private final SpeedMode speedMode;

    private final Gameboy gameboy;

    private final Optional<Console> console;

    private JFrame mainWindow;

    public Emulator(String[] args, Properties properties) throws IOException, JMSException {
        options = parseArgs(args);
        rom = new Cartridge(options);
        speedMode = new SpeedMode();
        serialEndpoint = SerialEndpoint.NULL_ENDPOINT;
        console = options.isDebug() ? Optional.of(new Console()) : Optional.empty();
        console.map(Thread::new).ifPresent(Thread::start);

        if (options.isHeadless()) {
            sound = null;
            display = null;
            guiController = null;
            queueController = new QueueController(properties);
            gameboy = new Gameboy(options, rom, Display.NULL_DISPLAY, queueController, SoundOutput.NULL_OUTPUT, serialEndpoint, console);
        } else {
            sound = new AudioSystemSoundOutput();
            display = new SwingDisplay(SCALE);
            guiController = null;
            queueController = new QueueController(properties);
//          guiController = new SwingController(properties);
            configureJms(queueController);
                        
            gameboy = new Gameboy(options, rom, display, queueController, sound, serialEndpoint, console);
        }
        console.ifPresent(c -> c.init(gameboy));
    }

    private static GameboyOptions parseArgs(String[] args) {
        if (args.length == 0) {
            GameboyOptions.printUsage(System.out);
            System.exit(0);
            return null;
        }
        try {
            return createGameboyOptions(args);
        } catch(IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println();
            GameboyOptions.printUsage(System.err);
            System.exit(1);
            return null;
        }
    }

    private static GameboyOptions createGameboyOptions(String[] args) {
        Set<String> params = new HashSet<>();
        Set<String> shortParams = new HashSet<>();
        String romPath = null;
        for (String a : args) {
            if (a.startsWith("--")) {
                params.add(a.substring(2));
            } else if (a.startsWith("-")) {
                shortParams.add(a.substring(1));
            } else {
                romPath = a;
            }
        }
        if (romPath == null) {
            throw new IllegalArgumentException("ROM path hasn't been specified");
        }
        File romFile = new File(romPath);
        if (!romFile.exists()) {
            throw new IllegalArgumentException("The ROM path doesn't exist: " + romPath);
        }
        return new GameboyOptions(romFile, params, shortParams);
    }

    public void run() throws Exception {
        if (options.isHeadless()) {
            gameboy.run();
        } else {
            System.setProperty("sun.java2d.opengl", "true");

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(() -> startGui());
        }
    }

    private void startGui() {
        display.setPreferredSize(new Dimension(160 * SCALE, 144 * SCALE));

        mainWindow = new JFrame("Coffee GB: " + rom.getTitle());
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLocationRelativeTo(null);

        mainWindow.setContentPane(display);
        mainWindow.setResizable(false);
        mainWindow.setVisible(true);
        mainWindow.pack();

        if(options.isHeadless()) {
            mainWindow.addKeyListener(guiController);
        }

        new Thread(display).start();
        new Thread(gameboy).start();
    }

    private void stopGui() {
        display.stop();
        gameboy.stop();
        mainWindow.dispose();
    }
    
    private void configureJms(QueueController controllerHook) throws JMSException {
        JmsConfiguration jmsConfig = new JmsConfiguration(BROKER_URL, INSTRUCTION_QUEUE);
        jmsConfig.getSession().createConsumer(jmsConfig.getInstructionsQueue())
        	.setMessageListener(new ActionListener(controllerHook));
        jmsConfig.getConnection().start();
    	
    }
}
