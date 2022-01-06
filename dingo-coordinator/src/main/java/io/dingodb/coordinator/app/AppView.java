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

package io.dingodb.coordinator.app;

import io.dingodb.coordinator.GeneralId;
import io.dingodb.coordinator.score.Score;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface AppView<V extends AppView<V, A>, A extends App<A, V>> extends Serializable {

    GeneralId viewId();

    GeneralId app();

    GeneralId leader();

    Set<GeneralId> nodeResources();

    Score score();

}
