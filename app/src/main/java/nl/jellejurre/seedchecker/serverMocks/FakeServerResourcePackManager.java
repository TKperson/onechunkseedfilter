package nl.jellejurre.seedchecker.serverMocks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.client.resource.server.DownloadQueuer;
import net.minecraft.client.resource.server.PackStateChangeCallback;
import net.minecraft.client.resource.server.ReloadScheduler;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Downloader;
import net.minecraft.util.Unit;

public class FakeServerResourcePackManager extends ServerResourcePackManager  {
    private static final CompletableFuture<Unit> COMPLETED_UNIT =  CompletableFuture.completedFuture(Unit.INSTANCE);
    private ReloadableResourceManagerImpl resourceManager;
    private TagManagerLoader registryTagManager;

    public FakeServerResourcePackManager() {
        super(
                new DownloadQueuer() {
                    @Override
                    public void enqueue(Map<UUID, Downloader.DownloadEntry> entries, Consumer<Downloader.DownloadResult> callback) {
                        return;
                    }
                },
                new PackStateChangeCallback() {
                    @Override
                    public void onStateChanged(UUID id, State state) {
                        return;
                    }

                    @Override
                    public void onFinish(UUID id, FinishState state) {
                        return;
                    }
                },
                new ReloadScheduler() {
                    @Override
                    public void scheduleReload(ReloadContext context) {
                        return;
                    }
                },
                () -> {},
                AcceptanceStatus.ALLOWED
        );
    }

    public void initialise(){
        this.resourceManager = new ReloadableResourceManagerImpl(ResourceType.SERVER_DATA);
        this.resourceManager.registerReloader(this.registryTagManager);
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void close() {
        this.resourceManager.close();
    }
}

