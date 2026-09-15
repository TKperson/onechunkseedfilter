package nl.jellejurre.seedchecker.serverMocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.registry.*;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.*;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.profiler.DummyRecorder;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerTiming;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.GameMode;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorage;
import nl.jellejurre.seedchecker.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

public class FakeMinecraftServer extends MinecraftServer {
    record ResourceManagerHolder(
            LifecycledResourceManager resourceManager,
            DataPackContents dataPackContents
    ) implements AutoCloseable {
        public void close() {
            this.resourceManager.close();
        }
    }

    //private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA = "vanilla";
    private static final float field_33212 = 0.8F;
    private static final int field_33213 = 100;
    //private static final long OVERLOAD_THRESHOLD_NANOS;
    private static final int field_47144 = 20;
    //private static final long OVERLOAD_WARNING_INTERVAL_NANOS;
    private static final int field_47146 = 100;
    //private static final long PLAYER_SAMPLE_UPDATE_INTERVAL_NANOS;
    //private static final long PREPARE_START_REGION_TICK_DELAY_NANOS;
    private static final int field_33218 = 12;
    public static final int START_TICKET_CHUNK_RADIUS = 11;
    private static final int START_TICKET_CHUNKS = 441;
    private static final int field_33220 = 6000;
    private static final int field_47149 = 100;
    private static final int field_33221 = 3;
    public static final int MAX_WORLD_BORDER_RADIUS = 29999984;
    //public static final LevelInfo DEMO_LEVEL_INFO;
    //public static final GameProfile ANONYMOUS_PLAYER_PROFILE;
    protected LevelStorage.Session session;
    protected WorldSaveHandler saveHandler;
    private final List<Runnable> serverGuiTickables = Lists.newArrayList();
    private Recorder recorder;
    private Profiler profiler;
    private Consumer<ProfileResult> recorderResultConsumer;
    private Consumer<Path> recorderDumpConsumer;
    private boolean needsRecorderSetup;
    @Nullable
    //private DebugStart debugStart;
    private boolean needsDebugSetup;
    private ServerNetworkIo networkIo;
    private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
    @Nullable
    private ServerMetadata metadata;
    @Nullable
    private ServerMetadata.Favicon favicon;
    private Random random;
    private DataFixer dataFixer;
    private String serverIp;
    private int serverPort;
    private CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries;
    private Map<RegistryKey<World>, ServerWorld> worlds;
    private PlayerManager playerManager;
    private volatile boolean running;
    private boolean stopped;
    private int ticks;
    private int ticksUntilAutosave;
    protected final Proxy proxy = Proxy.NO_PROXY;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvpEnabled;
    private boolean flightEnabled;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    //private final long[] tickTimes;
    private long recentTickTimesNanos;
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private GameProfile hostProfile;
    private boolean demo;
    private volatile boolean loading;
    private long lastOverloadWarningNanos;
    protected ApiServices apiServices;
    private long lastPlayerSampleUpdate;
    private final Thread serverThread = null;
    private long tickStartTimeNanos;
    private long tickEndTimeNanos;
    private boolean waitingForNextTick;
    private ResourcePackManager dataPackManager;
    private ServerScoreboard scoreboard;
    @Nullable
    private DataCommandStorage dataCommandStorage;
    private BossBarManager bossBarManager;
    private CommandFunctionManager commandFunctionManager;
    private boolean enforceWhitelist;
    private float averageTickTime;
    private Executor workerExecutor;
    @Nullable
    private String serverId;
    private ResourceManagerHolder resourceManagerHolder;
    private StructureTemplateManager structureTemplateManager;
    private ServerTickManager tickManager;
    protected SaveProperties saveProperties;
    private volatile boolean saving;

    public FakeMinecraftServer(
            LevelStorage.Session levelStorageSession,
            ResourcePackManager resourcePackManager,
            SaveLoader saveLoader,
            ApiServices apiServices,
            WorldGenerationProgressListenerFactory factory
    ) {
        super(
                null,
                levelStorageSession,
                resourcePackManager,
                saveLoader,
                Proxy.NO_PROXY,
                Schemas.getFixer(),
                apiServices,
                factory
        );

        this.playerManager = new FakePlayerManager(this, null, null, 101);
    }
    
    public static FakeMinecraftServer getMinecraftServer(
            LevelStorage.Session levelStorageSession,
            ResourcePackManager resourcePackManager,
            SaveLoader saveLoader,
            ApiServices apiServices,
            ServerResourcePackManager serverResourcePackManager,
            WorldGenerationProgressListenerFactory factory
    ){
        try {
            FakeMinecraftServer fakeMinecraftServer;
            fakeMinecraftServer = (FakeMinecraftServer) ReflectionUtils.unsafe.allocateInstance(FakeMinecraftServer.class);
            fakeMinecraftServer.initialise(
                    null,
                    levelStorageSession,
                    resourcePackManager,
                    saveLoader,
                    apiServices,
                    Proxy.NO_PROXY,
                    Schemas.getFixer(),
                    serverResourcePackManager,
                    factory
            );
            return fakeMinecraftServer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public GameMode getDefaultGameMode() {
        return this.saveProperties.getGameMode();
    }

    @Override
    public StructureTemplateManager getStructureTemplateManager() {
        return this.structureTemplateManager;
    }

    @Override
    public void executeTask(ServerTask task) {
        super.executeTask(task);
    }

    @Override
    public SystemDetails addExtraSystemDetails(SystemDetails details) {
        return null;
    }

    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }

    @Override
    public DynamicRegistryManager.Immutable getRegistryManager() {
        return this.combinedDynamicRegistries.getCombinedRegistryManager();
    }

    public void initialise(
            Thread serverThread,
            LevelStorage.Session levelStorageSession,
            ResourcePackManager resourcePackManager,
            SaveLoader saveLoader,
            ApiServices apiServices,
            Proxy proxy,
            DataFixer dataFixer,
            ServerResourcePackManager serverResourceManager,
            WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory
    ){
        this.ticks = 0;
        this.profiler = DummyRecorder.INSTANCE.getProfiler();

        //ServerMetadata.Players players = this.createMetadataPlayers();
        ServerMetadata.Players players = new ServerMetadata.Players(
                255,
                0,
                new ObjectArrayList<>()
        );
        this.metadata = new ServerMetadata(
                Text.of(""),
                Optional.of(players),
                Optional.of(ServerMetadata.Version.create()),
                Optional.empty(),
                this.shouldEnforceSecureProfile()
        );

        this.random = new Random();
        this.serverPort = -1;
        this.worlds = Maps.newLinkedHashMap();
        this.running = true;
        //ReflectionUtils.setValueOfField(this, "lastTickLengths", "lastTickLengths", new long[100]);
        //this.resourcePackUrl = "";
        //this.resourcePackHash = "";
        //this.timeReference = Util.getMeasuringTimeMs();
        this.scoreboard = new ServerScoreboard(this);
        this.bossBarManager = new BossBarManager();
        //this.metricsData = new MetricsData();
        //ReflectionUtils.setValueOfField(this, "registryManager", "registryManager", registryManager);
        //ReflectionUtils.setValueOfField(this, "saveProperties", "saveProperties",  saveProperties);
        this.dataPackManager = new ResourcePackManager();
        //this.serverResourceManager = serverResourceManager;
        //this.sessionService = sessionService;
        //this.gameProfileRepo = gameProfileRepo;
        //this.userCache = userCache;
        //if (userCache != null) {
        //    userCache.setExecutor(this);
        //}

        this.networkIo = new ServerNetworkIo(this);
        this.worldGenerationProgressListenerFactory = worldGenerationProgressListenerFactory;
        ReflectionUtils.setValueOfField(this, "session", "session", session);
        ReflectionUtils.setValueOfField(this, "saveHandler", "saveHandler", session.createSaveHandler());
        this.dataFixer = dataFixer;
        RegistryEntryLookup<Block> registryEntryLookup = this.combinedDynamicRegistries.getCombinedRegistryManager().get(RegistryKeys.BLOCK).getReadOnlyWrapper().withFeatureFilter(this.saveProperties.getEnabledFeatures());
        this.structureTemplateManager = new StructureTemplateManager(
                saveLoader.resourceManager(),
                session,
                dataFixer,
                registryEntryLookup
        );
        this.workerExecutor = Util.getMainWorkerExecutor();
        this.playerManager = new FakePlayerManager(this, null, null, 101);
    }

    @Override
    public SaveProperties getSaveProperties() {
        return saveProperties;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    protected boolean setupServer() throws IOException {
        return false;
    }

    @Override
    public int getOpPermissionLevel() {
        return 4;
    }

    @Override
    public int getFunctionPermissionLevel() {
        return 3;
    }

    @Override
    public boolean shouldBroadcastRconToOps() {
        return false;
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    @Override
    public int getRateLimit() {
        return 0;
    }

    @Override
    public boolean isUsingNativeTransport() {
        return false;
    }

    @Override
    public boolean areCommandBlocksEnabled() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean cannotBeSilenced() {
        return super.cannotBeSilenced();
    }

    @Override
    public boolean isHost(GameProfile profile) {
        return false;
    }

    @Override
    public <Source> CompletableFuture<Source> ask(
        Function<? super MessageListener<Source>, ? extends ServerTask> messageProvider) {
        return super.ask(messageProvider);
    }

    @Override
    public <Source> CompletableFuture<Source> askFallible(
        Function<? super MessageListener<Either<Source, Exception>>, ? extends ServerTask> messageProvider) {
        return super.askFallible(messageProvider);
    }


    public static class class_6414 {
        final long field_33980;
        final int field_33981;

        class_6414(long l, int i) {
            this.field_33980 = l;
            this.field_33981 = i;
        }

        ProfileResult method_37330(long l, int i) {
            return new ProfileResult() {
                public List<ProfilerTiming> getTimings(String parentPath) {
                    return Collections.emptyList();
                }

                public boolean save(Path path) {
                    return false;
                }

                public long getStartTime() {
                    return FakeMinecraftServer.class_6414.this.field_33980;
                }

                public int getStartTick() {
                    return FakeMinecraftServer.class_6414.this.field_33981;
                }

                public long getEndTime() {
                    return l;
                }

                public int getEndTick() {
                    return i;
                }

                public String getRootTimings() {
                    return "";
                }
            };
        }
    }

    @Override
    public boolean canExecute(ServerTask task){
        return true;
    };

    @Override
    protected ServerTask createTask(Runnable runnable){
        return null;
    };
}
