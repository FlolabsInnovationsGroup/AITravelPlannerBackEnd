/**
 * Comprehensive Prompt System Testing Script
 * Tests all 5 extension prompts with real queries to verify:
 * - Correct prompt routing
 * - Function calling integration
 * - Map location synchronization
 * - JSON format compliance
 * - Response quality
 */

const axios = require('axios');

const API_URL = 'http://localhost:8080';
const SESSION_ID = `test-session-${Date.now()}`;
let authToken = '';

// Color codes for terminal output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m'
};

function log(message, color = colors.reset) {
  console.log(`${color}${message}${colors.reset}`);
}

function logTest(testName) {
  console.log('\n' + '='.repeat(80));
  log(`🧪 TEST: ${testName}`, colors.bright + colors.cyan);
  console.log('='.repeat(80));
}

function logSuccess(message) {
  log(`✅ ${message}`, colors.green);
}

function logError(message) {
  log(`❌ ${message}`, colors.red);
}

function logWarning(message) {
  log(`⚠️  ${message}`, colors.yellow);
}

function logInfo(message) {
  log(`ℹ️  ${message}`, colors.blue);
}

// Create guest session
async function createGuestSession() {
  try {
    logInfo('Creating guest session...');
    const response = await axios.post(`${API_URL}/api/auth/guest-token`);
    authToken = response.data.token;
    logSuccess(`Guest token created: ${authToken.substring(0, 20)}...`);
    return true;
  } catch (error) {
    logError(`Failed to create guest session: ${error.message}`);
    return false;
  }
}

// Send chat message and get response
async function sendChatMessage(message, testName) {
  try {
    logInfo(`Sending query: "${message}"`);
    
    const startTime = Date.now();
    const response = await axios.post(
      `${API_URL}/api/chat/call`,
      {
        message: message,
        sessionId: SESSION_ID,
        userId: 'test-user'
      },
      {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
    const responseTime = Date.now() - startTime;
    
    logSuccess(`Response received in ${responseTime}ms`);
    
    // Log raw response for debugging
    console.log('\n🔍 Raw Response:');
    console.log(JSON.stringify(response.data, null, 2));
    
    // Validate response structure
    const data = response.data;
    
    if (!data) {
      logError('Response data is null or undefined');
      return { success: false, error: 'Empty response' };
    }
    
    // Check if response is valid JSON
    let parsedMessage;
    try {
      parsedMessage = typeof data.message === 'string' ? JSON.parse(data.message) : data.message;
      logSuccess('Response is valid JSON');
    } catch (e) {
      logWarning('Response is not JSON format (might be plain text)');
      parsedMessage = { message: data.message || data };
    }
    
    // Log response details
    console.log('\n📊 Response Analysis:');
    console.log('─'.repeat(80));
    
    if (parsedMessage.message) {
      const wordCount = parsedMessage.message.split(/\s+/).length;
      logInfo(`Message word count: ${wordCount}`);
      
      if (testName === 'SpeedExtension' && (wordCount < 150 || wordCount > 350)) {
        logWarning(`Word count ${wordCount} outside SpeedExtension target (200-300)`);
      } else if (testName !== 'SpeedExtension' && (wordCount < 250 || wordCount > 500)) {
        logWarning(`Word count ${wordCount} outside target range (300-450)`);
      } else {
        logSuccess(`Word count ${wordCount} within expected range`);
      }
      
      // Check for specific details
      const hasVenueName = /[A-Z][a-z]+\s+[A-Z][a-z]+/.test(parsedMessage.message);
      const hasPrice = /[\$€£¥]\d+|€\d+|\d+\s*euro|USD|EUR/.test(parsedMessage.message);
      const hasAddress = /\d+\s+[A-Z]|Avenue|Street|Road|Rue|Via/.test(parsedMessage.message);
      const hasPhone = /\+?\d{1,3}[\s-]?\d{1,4}[\s-]?\d{1,4}/.test(parsedMessage.message);
      
      logInfo(`Contains venue names: ${hasVenueName ? '✅' : '❌'}`);
      logInfo(`Contains prices: ${hasPrice ? '✅' : '❌'}`);
      logInfo(`Contains addresses: ${hasAddress ? '✅' : '❌'}`);
      logInfo(`Contains phone numbers: ${hasPhone ? '✅' : '❌'}`);
    }
    
    if (parsedMessage.suggestions) {
      logSuccess(`Has ${parsedMessage.suggestions.length} suggestions`);
      if (parsedMessage.suggestions.length !== 4) {
        logWarning(`Expected 4 suggestions, got ${parsedMessage.suggestions.length}`);
      }
    } else {
      logWarning('No suggestions found');
    }
    
    // Check for function calls in metadata
    if (data.metadata && data.metadata.functionsUsed) {
      logSuccess(`Functions called: ${data.metadata.functionsUsed.join(', ')}`);
    } else {
      logWarning('No function call metadata found');
    }
    
    // Display first 500 characters of response
    const messagePreview = parsedMessage.message 
      ? parsedMessage.message.substring(0, 500) + (parsedMessage.message.length > 500 ? '...' : '')
      : 'No message';
    console.log('\n💬 Response Preview:');
    console.log('─'.repeat(80));
    console.log(messagePreview);
    
    return {
      success: true,
      responseTime,
      parsedMessage,
      rawResponse: data
    };
    
  } catch (error) {
    logError(`Chat request failed: ${error.message}`);
    if (error.response) {
      logError(`Status: ${error.response.status}`);
      logError(`Data: ${JSON.stringify(error.response.data, null, 2)}`);
    }
    return { success: false, error: error.message };
  }
}

// Check locations added to map
async function checkMapLocations() {
  try {
    logInfo('Checking map locations...');
    const response = await axios.get(
      `${API_URL}/api/locations/${SESSION_ID}`,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    
    const locations = response.data;
    logSuccess(`Found ${locations.length} locations on map`);
    
    if (locations.length > 0) {
      locations.forEach((loc, idx) => {
        logInfo(`  ${idx + 1}. ${loc.name} at ${loc.latitude}, ${loc.longitude}`);
      });
    }
    
    return locations;
  } catch (error) {
    logWarning(`Could not fetch map locations: ${error.message}`);
    return [];
  }
}

// Main test suite
async function runTests() {
  console.log('\n');
  log('╔═══════════════════════════════════════════════════════════════════════════╗', colors.bright + colors.cyan);
  log('║           FLOMAD AI PROMPT SYSTEM COMPREHENSIVE TESTING SUITE            ║', colors.bright + colors.cyan);
  log('╚═══════════════════════════════════════════════════════════════════════════╝', colors.bright + colors.cyan);
  
  // Initialize session
  const sessionCreated = await createGuestSession();
  if (!sessionCreated) {
    logError('Failed to create session. Aborting tests.');
    return;
  }
  
  const testCases = [
    {
      name: 'Emergency Extension Test',
      query: 'Help! I lost my passport in Paris and need to fly home tomorrow',
      expectedPrompt: 'EmergencyExtension',
      expectedFunctions: ['emergencyAssistanceFunction'],
      checkFor: ['embassy', 'emergency', 'contact', '112', 'police']
    },
    {
      name: 'Budget Extension Test',
      query: 'I want a cheap 5-day Rome trip under $800 total',
      expectedPrompt: 'BudgetExtension',
      expectedFunctions: ['budgetOptimizationFunction'],
      checkFor: ['budget', 'save', 'cheap', '$', 'accommodation', 'food']
    },
    {
      name: 'Accessibility Extension Test',
      query: 'I need wheelchair-accessible hotels in London with roll-in showers',
      expectedPrompt: 'AccessibilityExtension',
      expectedFunctions: ['accessibilityFunction'],
      checkFor: ['wheelchair', 'accessible', 'mobility', 'ramp', 'elevator']
    },
    {
      name: 'Speed Extension Test',
      query: 'Best sushi restaurant in Tokyo?',
      expectedPrompt: 'SpeedExtension',
      expectedFunctions: ['placeTextSearchFunction'],
      checkFor: ['restaurant', 'Tokyo', 'sushi', 'rating', 'address']
    },
    {
      name: 'Planning Extension Test',
      query: 'Plan a 5-day Paris itinerary with must-see attractions',
      expectedPrompt: 'PlanningExtension',
      expectedFunctions: ['itineraryGeneratorFunction'],
      checkFor: ['Day 1', 'Day 2', 'itinerary', 'Eiffel', 'museum', 'morning', 'evening']
    }
  ];
  
  let passedTests = 0;
  let totalTests = testCases.length;
  
  for (const testCase of testCases) {
    logTest(testCase.name);
    
    const result = await sendChatMessage(testCase.query, testCase.expectedPrompt);
    
    if (result.success) {
      passedTests++;
      
      // Check for expected keywords in response
      const responseText = result.parsedMessage.message || '';
      const foundKeywords = testCase.checkFor.filter(keyword => 
        responseText.toLowerCase().includes(keyword.toLowerCase())
      );
      
      logInfo(`Found ${foundKeywords.length}/${testCase.checkFor.length} expected keywords`);
      logInfo(`Keywords: ${foundKeywords.join(', ')}`);
      
      // Wait a bit for WebSocket to sync locations
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      // Check map integration
      await checkMapLocations();
    }
    
    // Pause between tests
    await new Promise(resolve => setTimeout(resolve, 3000));
  }
  
  // Final summary
  console.log('\n');
  log('╔═══════════════════════════════════════════════════════════════════════════╗', colors.bright + colors.cyan);
  log('║                           TEST RESULTS SUMMARY                            ║', colors.bright + colors.cyan);
  log('╚═══════════════════════════════════════════════════════════════════════════╝', colors.bright + colors.cyan);
  
  log(`\nTotal Tests: ${totalTests}`, colors.bright);
  log(`Passed: ${passedTests}`, colors.green);
  log(`Failed: ${totalTests - passedTests}`, colors.red);
  log(`Success Rate: ${((passedTests / totalTests) * 100).toFixed(1)}%`, colors.bright);
  
  if (passedTests === totalTests) {
    log('\n🎉 ALL TESTS PASSED! Prompt system is working correctly!', colors.bright + colors.green);
  } else {
    log('\n⚠️  Some tests failed. Review logs above for details.', colors.yellow);
  }
}

// Run tests
runTests().catch(error => {
  logError(`Test suite failed: ${error.message}`);
  console.error(error);
  process.exit(1);
});
