
import './App.css'

function App() {
  return (
    <>
    <header>
      <h1>OpenAPI security scanner</h1>
      <p className="subtitle">Static security analysis for OpenAPI specifications</p>
    </header>
    
    <div className='info'>
      <div className='info-block'>
        <h3>What this tool DO</h3>
        <ul>
          <li>Scans OpenAPI spec for security</li>
          <li>Makes report on security level</li>
          <li>Identifies missing authentication</li>
        </ul>
      </div>

      <div className='info-block'>
        <h3>What this tool does NOT do</h3>
        <ul>
          <li>Runtime testing or penetration testing</li>
          <li>Store or modify your API specifications</li>
          <li>Access your actual API endpoints</li>
        </ul>
      </div>
    </div>

    <div className='scan-section'>
      <div className='scan-box'>
        <input 
          type="text"
          placeholder="Enter OpenAPI specification URL..."  />
        <button>Scan</button>
      </div>
    </div>
    </>
  )
}

export default App
