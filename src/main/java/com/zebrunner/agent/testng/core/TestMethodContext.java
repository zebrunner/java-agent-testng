package com.zebrunner.agent.testng.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Stores context of specific test method (not to be confused with specific test execution) belonging to specific test class instance
 */
public class TestMethodContext {

    /**
     * Represents current test method invocation. 0 means that method was not invoked yet.
     */
    private final ThreadLocal<AtomicInteger> currentInvocationCount
            = ThreadLocal.withInitial(() -> new AtomicInteger(0));

    @Getter
    private List<Integer> dataProviderIndicesForRerun = Collections.emptyList();
    private List<Object[]> dataProviderData = Collections.emptyList();
    private final ThreadLocal<DataProviderData> currentDataProviderData = new ThreadLocal<>();
    private final ThreadLocal<Integer> currentDataProviderIteratorIndex = ThreadLocal.withInitial(() -> -1);

    public void setDataProviderIndicesForRerun(Collection<Integer> indicesForRerun) {
        dataProviderIndicesForRerun = new ArrayList<>(indicesForRerun);
        Collections.sort(dataProviderIndicesForRerun);
    }

    public int getDataProviderSize() {
        return dataProviderData.size();
    }

    public void setDataProviderData(List<Object[]> dataProviderData) {
        if (dataProviderData != null) {
            this.dataProviderData = dataProviderData;
        }
    }

    public void setCurrentDataProviderIteratorIndex(int currentDataProviderIteratorIndex) {
        this.currentDataProviderIteratorIndex.set(currentDataProviderIteratorIndex);
    }

    public void setCurrentDataProviderData(Object[] parameters, Integer index) {
        this.currentDataProviderData.set(new DataProviderData(parameters, index));
    }

    public int getCurrentDataProviderIndex(Object[] actualTestParameters) {
        return this.getCurrentDataProviderIteratorIndex()
                   .filter(currentIndex -> currentIndex != -1)
                   // the checks are performed for cases
                   // when data provider data is loaded in one thread but is used in another one.
                   // in such cases we try to find data provider line by matching
                   // the test method arguments or their values
                   .or(() -> this.getReferenceEqualDataProviderData(actualTestParameters))
                   .or(() -> this.getValueEqualDataProviderData(actualTestParameters))
                   .or(() -> this.getStringSameDataProviderData(actualTestParameters))
                   .or(() -> this.getIndexOfMatchingDataProviderData(actualTestParameters))
                   .orElse(-1);
    }

    public Optional<Integer> getCurrentDataProviderIteratorIndex() {
        Integer currentIndex = currentDataProviderIteratorIndex.get();

        return currentIndex > -1 && dataProviderIndicesForRerun.size() > currentIndex
                ? Optional.ofNullable(dataProviderIndicesForRerun.get(currentIndex))
                : Optional.of(currentIndex);
    }

    public Optional<Integer> getReferenceEqualDataProviderData(Object[] data) {
        for (int i = 0; i < dataProviderData.size(); i++) {
            if (dataProviderData.get(i) == data) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public Optional<Integer> getValueEqualDataProviderData(Object[] data) {
        List<Object> dataAsList = Arrays.asList(data);

        for (int i = 0; i < dataProviderData.size(); i++) {
            List<Object> dataProviderLineAsList = Arrays.asList(dataProviderData.get(i));

            if (dataProviderLineAsList.equals(dataAsList)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public Optional<Integer> getStringSameDataProviderData(Object[] data) {
        List<String> dataAsList = this.toStringsList(data);

        for (int i = 0; i < dataProviderData.size(); i++) {
            List<String> dataProviderLineAsList = this.toStringsList(dataProviderData.get(i));

            if (dataProviderLineAsList.equals(dataAsList)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    private List<String> toStringsList(Object[] data) {
        return Arrays.stream(data)
                     .map(Objects::toString)
                     .collect(Collectors.toList());
    }

    private Optional<Integer> getIndexOfMatchingDataProviderData(Object[] actualTestParameters) {
        DataProviderData currentDataProviderData = this.currentDataProviderData.get();

        if (currentDataProviderData != null && Arrays.equals(currentDataProviderData.parameters, actualTestParameters)) {
            return Optional.ofNullable(currentDataProviderData.index);
        }

        return Optional.empty();
    }

    public void incrementInvocationIndex() {
        currentInvocationCount.get().incrementAndGet();
    }

    public int getCurrentInvocationIndex() {
        return currentInvocationCount.get().get();
    }

    @RequiredArgsConstructor
    private static class DataProviderData {

        private final Object[] parameters;
        private final Integer index;

    }

}
