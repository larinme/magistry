package ru.ifmo.utils;

import com.google.inject.AbstractModule;
import ru.ifmo.utils.impl.DialogueBuilderImpl;
import ru.ifmo.utils.impl.DialogueWriterImpl;
import ru.ifmo.utils.impl.DocumentDownloaderImpl;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(DialogueBuilder.class).to(DialogueBuilderImpl.class);
        bind(DocumentDownloader.class).to(DocumentDownloaderImpl.class);
        bind(DialogueWriter.class).to(DialogueWriterImpl.class);
    }
}
