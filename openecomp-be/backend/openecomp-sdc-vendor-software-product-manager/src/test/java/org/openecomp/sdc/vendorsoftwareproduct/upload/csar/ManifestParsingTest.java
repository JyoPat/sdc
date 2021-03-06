/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.OnboardingManifest;

import java.io.IOException;
import java.io.InputStream;
import org.testng.annotations.Test;


public class ManifestParsingTest {

  @Test
  public void testSuccessfulParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/ValidTosca.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertTrue(onboardingManifest.isValid());
      assertEquals(onboardingManifest.getMetadata().size(), 4);
      assertEquals(onboardingManifest.getSources().size(), 5);
    }
  }

  @Test
  public void testNoMetadataParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca1.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testBrokenMDParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca2.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testNoMetaParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca4.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_NO_METADATA.getErrorMessage().substring(0, 10))));
    }
  }


}
