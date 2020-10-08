package com.zebrunner.agent.testng.listener;

import com.google.common.collect.Lists;
import com.zebrunner.agent.core.registrar.RerunContextHolder;
import lombok.RequiredArgsConstructor;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DataProviderInterceptor implements IDataProviderInterceptor {

    @Override
    public Iterator<Object[]> intercept(Iterator<Object[]> original, IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext context) {
        Iterator<Object[]> result;
        if (RerunContextHolder.isRerun()) {
            Set<Integer> indices = RunContextService.getDataProviderIndicesForRerun(method, context);
            boolean forced = RunContextService.isForceRerun(method, context);
            boolean filterIndices = !indices.isEmpty() && !forced;

            result = filterIndices ? filterDataProviderIndices(original, indices) : original;
        } else {
            result = original;
        }
        List<Object[]> resultAsList = Lists.newArrayList(result);
        RunContextService.setDataProviderSize(method, context, resultAsList.size());

        return new TrackableIterator(resultAsList.iterator(), method, context);
    }

    private Iterator<Object[]> filterDataProviderIndices(Iterator<Object[]> original, Set<Integer> rerunIndices) {
        return new Iterator<Object[]>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return original.hasNext() && rerunIndices.contains(index);
            }

            @Override
            public Object[] next() {
                Object[] result = null;
                if (rerunIndices.contains(index)) {
                    result = original.next();
                    index++;
                }
                return result;
            }

        };
    }

    @RequiredArgsConstructor
    private static class TrackableIterator implements Iterator<Object[]> {

        private final Iterator<Object[]> originalIterator;
        private final ITestNGMethod method;
        private final ITestContext context;
        private int parameterIndex = 0;

        @Override
        public boolean hasNext() {
            return originalIterator.hasNext();
        }

        @Override
        public Object[] next() {
            RunContextService.setDataProviderCurrentIndex(method, context, parameterIndex++);
            return originalIterator.next();
        }

    }

}
