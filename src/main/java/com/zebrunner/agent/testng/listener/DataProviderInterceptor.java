package com.zebrunner.agent.testng.listener;

import com.zebrunner.agent.core.registrar.RerunContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class DataProviderInterceptor implements IDataProviderInterceptor {

    @Override
    public Iterator<Object[]> intercept(Iterator<Object[]> original,
                                        IDataProviderMethod dataProviderMethod,
                                        ITestNGMethod method,
                                        ITestContext context) {
        log.debug("Injecting DataProviderInterceptor -> intercept");
        // there is an issue with TestNG that in some cases
        // a IDataProviderInterceptor instance can be registered and invoked two or more times in a row.
        // in order to not perform filtration many times, we check type of the original iterator here
        if (original instanceof TrackableIterator) {
            return original;
        } else {
            List<Object[]> dataProviderData = this.toArrayList(original);
            RunContextService.setDataProviderData(method, context, dataProviderData);

            if (RerunContextHolder.isRerun()) {
                List<Integer> indicesForRerun = RunContextService.getDataProviderIndicesForRerun(method, context);
                if (!indicesForRerun.isEmpty()) {
                    dataProviderData = filterDataProviderData(dataProviderData, indicesForRerun);
                }
            }

            return new TrackableIterator(dataProviderData.iterator(), method, context);
        }
    }

    private <T> List<T> toArrayList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    private static List<Object[]> filterDataProviderData(List<Object[]> dataProviderData, List<Integer> indicesForRerun) {
        List<Object[]> filteredData = new ArrayList<>();

        for (Integer index : indicesForRerun) {
            if (index < dataProviderData.size()) {
                filteredData.add(dataProviderData.get(index));
            }
        }

        return filteredData;
    }

    @RequiredArgsConstructor
    private static class TrackableIterator implements Iterator<Object[]> {

        private final Iterator<Object[]> originalIterator;
        private final ITestNGMethod method;
        private final ITestContext context;
        private int index = 0;

        @Override
        public boolean hasNext() {
            return originalIterator.hasNext();
        }

        @Override
        public Object[] next() {
            // there is a very subtle trait of testng that lead to this check before setting current data provider index.
            //
            // when tests run sequentially, the same thread is used to iterate through the data provider data and to execute the test.
            // in such cases we don't have any difficulties in figuring out what is the current line of the data provider.
            //
            // when tests run in parallel, testng load data provider data within one thread (basically in main)
            // but the tests are executed another pooled threads.
            // because of this we have to use another approach then for sequential run.
            // for more information check the com.zebrunner.agent.testng.core.TestMethodContext.getCurrentDataProviderIndex
            //
            // if any of the tests fails when they run in parallel and there is a IRetryAnalyzer for the test,
            // then testng loads data provider again and refreshes the line for the failed test.
            // this is performed by iterating through the iterator.
            // from the agent implementation standpoint,
            // this is a false-positive invocation of the setCurrentDataProviderIteratorIndex method.
            if (RetryService.isRetryFinished(method, context)) {
                RunContextService.setCurrentDataProviderIteratorIndex(method, context, index++);
            }
            return originalIterator.next();
        }

    }

}
