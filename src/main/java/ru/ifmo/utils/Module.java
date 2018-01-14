package ru.ifmo.utils;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import ru.ifmo.utils.entity.DocumentDownloadingThread;
import ru.ifmo.utils.impl.DialogueBuilderImpl;
import ru.ifmo.utils.impl.DialogueWriterImpl;
import ru.ifmo.utils.impl.DocumentDownloaderImpl;
import ru.ifmo.utils.impl.ThreadRunnerImpl;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(DialogueBuilder.class).to(DialogueBuilderImpl.class);
        bind(DocumentDownloader.class).to(DocumentDownloaderImpl.class);
        bind(DialogueWriter.class).to(DialogueWriterImpl.class);
        bind(new TypeLiteral<ThreadRunner<DocumentDownloadingThread>>() {
        })
                .to(new TypeLiteral<ThreadRunnerImpl<DocumentDownloadingThread>>() {
                });
    }
}
