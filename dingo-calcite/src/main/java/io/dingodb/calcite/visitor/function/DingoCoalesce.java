/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.calcite.visitor.function;

import io.dingodb.exec.base.Id;
import io.dingodb.exec.base.IdGenerator;
import io.dingodb.exec.base.Operator;
import io.dingodb.exec.base.Output;
import io.dingodb.exec.base.Task;
import io.dingodb.exec.operator.CoalesceOperator;
import io.dingodb.exec.operator.SumUpOperator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DingoCoalesce {
    @NonNull
    public static List<Output> coalesce(IdGenerator idGenerator, @NonNull Collection<Output> inputs) {
        // Coalesce inputs from the same task. taskId --> list of inputs
        Map<Id, List<Output>> inputsMap = new HashMap<>();
        for (Output input : inputs) {
            Id taskId = input.getTaskId();
            List<Output> list = inputsMap.computeIfAbsent(taskId, k -> new LinkedList<>());
            list.add(input);
        }
        List<Output> outputs = new LinkedList<>();
        for (Map.Entry<Id, List<Output>> entry : inputsMap.entrySet()) {
            List<Output> list = entry.getValue();
            int size = list.size();
            if (size <= 1) {
                // Need no coalescing.
                outputs.addAll(list);
            } else {
                Output one = list.get(0);
                Task task = one.getTask();
                Operator operator = new CoalesceOperator(size);
                operator.setId(idGenerator.get());
                task.putOperator(operator);
                int i = 0;
                for (Output input : list) {
                    input.setLink(operator.getInput(i));
                    ++i;
                }
                Output newOutput = operator.getSoleOutput();
                newOutput.copyHint(one);
                if (one.isToSumUp()) {
                    Operator sumUpOperator = new SumUpOperator();
                    sumUpOperator.setId(idGenerator.get());
                    task.putOperator(sumUpOperator);
                    operator.getSoleOutput().setLink(sumUpOperator.getInput(0));
                    sumUpOperator.getSoleOutput().copyHint(newOutput);
                    outputs.add(sumUpOperator.getSoleOutput());
                } else {
                    outputs.add(newOutput);
                }
            }
        }
        return outputs;
    }
}
